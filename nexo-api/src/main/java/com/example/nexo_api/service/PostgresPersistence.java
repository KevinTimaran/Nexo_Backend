package com.example.nexo_api.service;

import com.example.nexo_api.model.Project;
import com.example.nexo_api.model.Workspace;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Cloud Postgres (Supabase, Neon, ...). Each project and each workspace is one JSONB row, so the
 * existing models are stored exactly as they are today in the JSON file.
 *
 * Configuration (environment variables):
 *   NEXO_DB_URL       jdbc:postgresql://host:5432/postgres?sslmode=require
 *                     or the plain URI Supabase shows: postgresql://user:password@host:5432/postgres
 *   NEXO_DB_USER      only when NEXO_DB_URL is a jdbc: URL
 *   NEXO_DB_PASSWORD  only when NEXO_DB_URL is a jdbc: URL
 */
public class PostgresPersistence implements PersistenceBackend, AutoCloseable {

    private static final String FLAG = "initialized";

    private final ObjectMapper mapper = new ObjectMapper();
    private final HikariDataSource dataSource;
    private final String description;

    public PostgresPersistence(String url, String user, String password) {
        String[] parsed = parse(url, user, password);
        String jdbcUrl = parsed[0];
        String username = parsed[1];
        String secret = parsed[2];
        if (!jdbcUrl.contains("sslmode=")) jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=require";

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(secret);
        config.setMaximumPoolSize(4);
        config.setConnectionTimeout(10_000);
        config.setPoolName("nexo-db");
        // Supabase's transaction pooler (port 6543) does not support prepared statement caching.
        config.addDataSourceProperty("prepareThreshold", "0");
        this.dataSource = new HikariDataSource(config);
        this.description = "postgres:" + URI.create(jdbcUrl.substring("jdbc:".length())).getHost();
        createSchema();
    }

    /**
     * Accepts a jdbc: URL (user/password passed separately) or the postgresql:// URI shown by Supabase.
     * The password may contain "@" even when not URL-encoded: the credentials end at the LAST "@".
     * Returns {jdbcUrl, user, password}.
     */
    static String[] parse(String url, String user, String password) {
        if (!(url.startsWith("postgres://") || url.startsWith("postgresql://"))) return new String[] {url, user, password};
        String rest = url.substring(url.indexOf("://") + 3);
        int at = rest.lastIndexOf('@');
        int colon = at > 0 ? rest.indexOf(':') : -1;
        if (at < 0 || colon < 0 || colon > at) {
            throw new IllegalArgumentException("NEXO_DB_URL debe incluir usuario y contraseña (postgresql://usuario:contraseña@host:5432/postgres)");
        }
        String username = URLDecoder.decode(rest.substring(0, colon), StandardCharsets.UTF_8);
        String secret = URLDecoder.decode(rest.substring(colon + 1, at).replace("+", "%2B"), StandardCharsets.UTF_8);
        String hostAndPath = rest.substring(at + 1);
        String query = "";
        int question = hostAndPath.indexOf('?');
        if (question >= 0) { query = hostAndPath.substring(question); hostAndPath = hostAndPath.substring(0, question); }
        int slash = hostAndPath.indexOf('/');
        String hostPort = slash >= 0 ? hostAndPath.substring(0, slash) : hostAndPath;
        String path = slash >= 0 ? hostAndPath.substring(slash) : "/postgres";
        if (!hostPort.contains(":")) hostPort += ":5432";
        return new String[] {"jdbc:postgresql://" + hostPort + path + query, username, secret};
    }

    private void createSchema() {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS nexo_projects (id TEXT PRIMARY KEY, data JSONB NOT NULL, updated_at TIMESTAMPTZ NOT NULL DEFAULT now())");
            s.execute("CREATE TABLE IF NOT EXISTS nexo_workspaces (project_id TEXT PRIMARY KEY, data JSONB NOT NULL, updated_at TIMESTAMPTZ NOT NULL DEFAULT now())");
            s.execute("CREATE TABLE IF NOT EXISTS nexo_meta (key TEXT PRIMARY KEY, value TEXT NOT NULL)");
            // Supabase exposes tables in the public schema through its REST API. Row Level Security with no
            // policies blocks that public access; this backend connects as the database owner, which bypasses it.
            s.execute("ALTER TABLE nexo_projects ENABLE ROW LEVEL SECURITY");
            s.execute("ALTER TABLE nexo_workspaces ENABLE ROW LEVEL SECURITY");
            s.execute("ALTER TABLE nexo_meta ENABLE ROW LEVEL SECURITY");
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo preparar la base de datos: " + ex.getMessage(), ex);
        }
    }

    @Override
    public ProjectService.PersistedData load() {
        try (Connection c = dataSource.getConnection()) {
            boolean initialized;
            try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM nexo_meta WHERE key = ?")) {
                ps.setString(1, FLAG);
                try (ResultSet rs = ps.executeQuery()) { initialized = rs.next(); }
            }
            if (!initialized) return null;
            ProjectService.PersistedData data = new ProjectService.PersistedData();
            data.projects = new HashMap<>();
            data.workspaces = new HashMap<>();
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT id, data::text FROM nexo_projects")) {
                while (rs.next()) data.projects.put(rs.getString(1), mapper.readValue(rs.getString(2), Project.class));
            }
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT project_id, data::text FROM nexo_workspaces")) {
                while (rs.next()) data.workspaces.put(rs.getString(1), mapper.readValue(rs.getString(2), Workspace.class));
            }
            return data;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudieron leer los proyectos de la base de datos", ex);
        }
    }

    @Override
    public void sync(Map<String, Project> projects, Map<String, Workspace> workspaces, String changedId) {
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try {
                if (changedId == null) {
                    // Full write (first run / import): replace everything in one transaction.
                    try (Statement s = c.createStatement()) {
                        s.execute("DELETE FROM nexo_workspaces");
                        s.execute("DELETE FROM nexo_projects");
                    }
                    for (String id : projects.keySet()) upsert(c, id, projects.get(id), workspaces.get(id));
                } else if (projects.containsKey(changedId)) {
                    upsert(c, changedId, projects.get(changedId), workspaces.get(changedId));
                } else {
                    delete(c, changedId);
                }
                c.commit();
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo guardar en la base de datos: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void initialize(Map<String, Project> projects, Map<String, Workspace> workspaces) {
        sync(projects, workspaces, null);
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO nexo_meta (key, value) VALUES (?, 'true') ON CONFLICT (key) DO NOTHING")) {
            ps.setString(1, FLAG);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo inicializar la base de datos", ex);
        }
    }

    private void upsert(Connection c, String id, Project project, Workspace workspace) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO nexo_projects (id, data) VALUES (?, ?::jsonb) ON CONFLICT (id) DO UPDATE SET data = EXCLUDED.data, updated_at = now()")) {
            ps.setString(1, id);
            ps.setString(2, mapper.writeValueAsString(project));
            ps.executeUpdate();
        }
        if (workspace != null) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO nexo_workspaces (project_id, data) VALUES (?, ?::jsonb) ON CONFLICT (project_id) DO UPDATE SET data = EXCLUDED.data, updated_at = now()")) {
                ps.setString(1, id);
                ps.setString(2, mapper.writeValueAsString(workspace));
                ps.executeUpdate();
            }
        }
    }

    private void delete(Connection c, String id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM nexo_workspaces WHERE project_id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM nexo_projects WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public String describe() {
        return description;
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
