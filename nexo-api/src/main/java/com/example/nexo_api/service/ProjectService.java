package com.example.nexo_api.service;

import com.example.nexo_api.config.AuthUser;
import com.example.nexo_api.model.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Projects and workspaces, owned by one user each. Every method takes the id of the signed-in user
 * (the JWT subject) and treats projects of other users as non-existent, so ids alone never grant access.
 */
@Service
public class ProjectService {

    private final Map<String, Project> projects = new ConcurrentHashMap<>();
    private final Map<String, Workspace> workspaces = new ConcurrentHashMap<>();
    private final PersistenceBackend backend;
    /** Local JSON file from earlier versions; imported once when a cloud database is empty. */
    private final PersistenceBackend legacyFile;
    /** Account that adopts projects created before ownership existed (NEXO_LEGACY_OWNER_ID); null = they stay hidden. */
    private final String legacyOwnerId;

    /** Spring uses this constructor: cloud Postgres when NEXO_DB_URL is set, otherwise the local JSON file. */
    public ProjectService() {
        this(selectBackend(), legacyDataFile(), legacyOwner());
    }

    /** File backend at a custom path (tests). */
    public ProjectService(Path dataFile) {
        this(new FilePersistence(dataFile), null, null);
    }

    public ProjectService(PersistenceBackend backend, PersistenceBackend legacyFile) {
        this(backend, legacyFile, null);
    }

    public ProjectService(PersistenceBackend backend, PersistenceBackend legacyFile, String legacyOwnerId) {
        this.backend = backend;
        this.legacyFile = legacyFile;
        this.legacyOwnerId = legacyOwnerId == null || legacyOwnerId.isBlank() ? null : legacyOwnerId;
    }

    private static Path defaultDataPath() {
        return Path.of(Optional.ofNullable(System.getenv("NEXO_DATA_FILE"))
                .filter(value -> !value.isBlank())
                .orElseGet(() -> Path.of(System.getProperty("user.home"), ".nexo", "data.json").toString()));
    }

    private static PersistenceBackend legacyDataFile() {
        if (System.getProperty("nexo.data.file") != null) return null;
        String url = System.getenv("NEXO_DB_URL");
        return url == null || url.isBlank() ? null : new FilePersistence(defaultDataPath());
    }

    private static PersistenceBackend selectBackend() {
        // Explicit file (tests): never touches the cloud database, whatever the environment says.
        String testFile = System.getProperty("nexo.data.file");
        if (testFile != null && !testFile.isBlank()) return new FilePersistence(Path.of(testFile));
        String url = System.getenv("NEXO_DB_URL");
        if (url == null || url.isBlank()) return new FilePersistence(defaultDataPath());
        return new PostgresPersistence(url.trim(), System.getenv("NEXO_DB_USER"), System.getenv("NEXO_DB_PASSWORD"));
    }

    /** Explicit owner for old projects, or the local user when authentication is disabled for local development. */
    private static String legacyOwner() {
        String configured = System.getenv("NEXO_LEGACY_OWNER_ID");
        if (configured != null && !configured.isBlank()) return configured.trim();
        return "true".equalsIgnoreCase(System.getenv("NEXO_AUTH_DISABLED")) ? AuthUser.LOCAL_USER : null;
    }

    public String storageDescription() { return backend.describe(); }

    public static class RevisionConflictException extends RuntimeException {
        private final Workspace current;
        public RevisionConflictException(Workspace current) { this.current = current; }
        public Workspace getCurrent() { return current; }
    }

    public static class PersistedData {
        public Map<String, Project> projects = new HashMap<>();
        public Map<String, Workspace> workspaces = new HashMap<>();
    }

    /** Persists a candidate state; memory is only updated by callers after this returns. */
    private void persistCandidate(Map<String, Project> nextProjects, Map<String, Workspace> nextWorkspaces, String changedId) {
        backend.sync(nextProjects, nextWorkspaces, changedId);
    }

    @PostConstruct
    public synchronized void init() {
        PersistedData data = backend.load();
        boolean firstRun = data == null;
        if (data == null && legacyFile != null) data = legacyFile.load();
        if (data != null) {
            projects.clear(); projects.putAll(data.projects);
            workspaces.clear(); workspaces.putAll(data.workspaces);
        }
        boolean adopted = adoptLegacyProjects();
        if (firstRun || adopted) {
            Map<String, Project> p = new HashMap<>(projects);
            Map<String, Workspace> w = new HashMap<>(workspaces);
            // First run on a cloud database: store what we have (imported file) and mark it initialised.
            if (firstRun) backend.initialize(p, w); else backend.sync(p, w, null);
        }
    }

    /** Gives ownerless projects to the configured legacy owner. Returns true when something changed. */
    private boolean adoptLegacyProjects() {
        if (legacyOwnerId == null) return false;
        boolean changed = false;
        for (Project project : projects.values()) {
            if (project.getOwnerId() == null) { project.setOwnerId(legacyOwnerId); changed = true; }
        }
        for (Workspace workspace : workspaces.values()) {
            if (workspace.getProject() != null && workspace.getProject().getOwnerId() == null) workspace.getProject().setOwnerId(legacyOwnerId);
        }
        return changed;
    }

    private Project owned(String userId, String id) {
        Project p = id == null ? null : projects.get(id);
        return p != null && userId != null && userId.equals(p.getOwnerId()) ? p : null;
    }

    public List<Project> listProjects(String userId, String lang, String status, String search) {
        List<Project> list = new ArrayList<>();
        for (Project p : projects.values()) if (userId != null && userId.equals(p.getOwnerId())) list.add(p);
        if (status != null && !status.isBlank() && !"all".equalsIgnoreCase(status)) {
            list.removeIf(p -> !status.equalsIgnoreCase(p.getStatus()));
        }
        if (search != null && !search.isBlank()) {
            String lower = search.toLowerCase();
            list.removeIf(p -> (p.getName() == null || !p.getName().toLowerCase().contains(lower)) &&
                               (p.getDescription() == null || !p.getDescription().toLowerCase().contains(lower)));
        }
        list.sort((a, b) -> {
            if (a.getUpdatedAt() == null) return 1;
            if (b.getUpdatedAt() == null) return -1;
            return b.getUpdatedAt().compareTo(a.getUpdatedAt());
        });
        return list;
    }

    public synchronized Project createProject(String userId, NewProjectRequest request) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("Usuario requerido");
        String id = "p-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String name = request.getName() != null && !request.getName().isBlank() ? request.getName().trim() : "Nuevo Proyecto";
        String description = request.getDescription() != null ? request.getDescription().trim() : "";
        String icon = request.getIcon() != null && !request.getIcon().isBlank() ? request.getIcon() : "lightbulb";
        String now = Instant.now().toString();

        Project project = new Project(id, name, description, "draft", icon, now, 0);
        project.setOwnerId(userId);
        Workspace workspace = new Workspace(project, new ConceptMap(new ArrayList<>(), new ArrayList<>()), new ArrayList<>());
        Map<String, Project> nextProjects = new HashMap<>(projects);
        Map<String, Workspace> nextWorkspaces = new HashMap<>(workspaces);
        nextProjects.put(id, project);
        nextWorkspaces.put(id, workspace);
        persistCandidate(nextProjects, nextWorkspaces, id);
        projects.put(id, project);
        workspaces.put(id, workspace);
        return project;
    }

    public Project getProject(String userId, String id) {
        return owned(userId, id);
    }

    public synchronized Project updateProject(String userId, String id, Map<String, Object> updates) {
        Project p = owned(userId, id);
        if (p == null) return null;

        Project next = copyOf(p);
        if (updates.containsKey("name") && updates.get("name") instanceof String s) next.setName(s);
        if (updates.containsKey("description") && updates.get("description") instanceof String s) next.setDescription(s);
        if (updates.containsKey("status") && updates.get("status") instanceof String s) next.setStatus(s);
        if (updates.containsKey("icon") && updates.get("icon") instanceof String s) next.setIcon(s);
        next.setUpdatedAt(Instant.now().toString());

        Map<String, Project> nextProjects = new HashMap<>(projects);
        nextProjects.put(id, next);
        Map<String, Workspace> nextWorkspaces = new HashMap<>(workspaces);
        Workspace current = nextWorkspaces.get(id);
        if (current != null) {
            Workspace updated = new Workspace(next, current.getMap(), current.getMessages());
            updated.setDiscovery(current.getDiscovery());
            updated.setRevision(current.getRevision());
            updated.setAppliedOperations(current.getAppliedOperations());
            nextWorkspaces.put(id, updated);
        }
        persistCandidate(nextProjects, nextWorkspaces, id);
        projects.put(id, next);
        if (current != null) workspaces.put(id, nextWorkspaces.get(id));
        return next;
    }

    private static Project copyOf(Project p) {
        Project copy = new Project(p.getId(), p.getName(), p.getDescription(), p.getStatus(), p.getIcon(), p.getUpdatedAt(), p.getNodeCount());
        copy.setOwnerId(p.getOwnerId());
        return copy;
    }

    /**
     * Deletes a project. The write happens first: if storage fails an exception is thrown and
     * nothing changes in memory, so the project never "comes back" after a restart.
     * Returns false when the project did not exist for this user (callers treat that as already deleted).
     */
    public synchronized boolean deleteProject(String userId, String id) {
        if (owned(userId, id) == null) return false;
        Map<String, Project> nextProjects = new HashMap<>(projects);
        Map<String, Workspace> nextWorkspaces = new HashMap<>(workspaces);
        nextProjects.remove(id);
        nextWorkspaces.remove(id);
        persistCandidate(nextProjects, nextWorkspaces, id);
        projects.remove(id);
        workspaces.remove(id);
        return true;
    }

    public Workspace getWorkspace(String userId, String projectId) {
        Project p = owned(userId, projectId);
        if (p == null) return null;

        Workspace ws = workspaces.get(projectId);
        if (ws == null) {
            ws = new Workspace(p, new ConceptMap(new ArrayList<>(), new ArrayList<>()), new ArrayList<>());
            workspaces.put(projectId, ws);
        }
        return ws;
    }

    public synchronized Workspace saveWorkspace(String userId, String projectId, UpdateWorkspaceRequest request) {
        Project p = owned(userId, projectId);
        if (p == null) return null;

        if (request.getExpectedRevision() == null || request.getOperationId() == null || request.getOperationId().isBlank())
            throw new IllegalArgumentException("expectedRevision y operationId son obligatorios");

        Workspace existing = getWorkspace(userId, projectId);
        if (existing.getAppliedOperations().containsKey(request.getOperationId())) return existing;
        if (existing.getRevision() != request.getExpectedRevision()) throw new RevisionConflictException(existing);
        ConceptMap map = request.getMap();
        List<ChatMessage> messages = request.getMessages();
        Project nextProject = copyOf(p);
        nextProject.setUpdatedAt(Instant.now().toString());
        Workspace ws = new Workspace(nextProject, map != null ? map : existing.getMap(), messages != null ? messages : existing.getMessages());
        ws.setDiscovery(request.getDiscovery() != null ? request.getDiscovery() : existing.getDiscovery());
        ws.setRevision(existing.getRevision() + 1);
        ws.setAppliedOperations(new LinkedHashMap<>(existing.getAppliedOperations()));
        ws.getAppliedOperations().put(request.getOperationId(), ws.getRevision());
        while (ws.getAppliedOperations().size() > 256) ws.getAppliedOperations().remove(ws.getAppliedOperations().keySet().iterator().next());

        int nodeCount = ws.getMap() != null && ws.getMap().getNodes() != null ? ws.getMap().getNodes().size() : 0;
        nextProject.setNodeCount(nodeCount);
        Map<String, Project> nextProjects = new HashMap<>(projects);
        Map<String, Workspace> nextWorkspaces = new HashMap<>(workspaces);
        nextProjects.put(projectId, nextProject);
        nextWorkspaces.put(projectId, ws);
        persistCandidate(nextProjects, nextWorkspaces, projectId);
        projects.put(projectId, nextProject);
        workspaces.put(projectId, ws);
        return ws;
    }
}
