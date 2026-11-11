package com.example.nexo_api.service;

import com.example.nexo_api.model.Project;
import com.example.nexo_api.model.Workspace;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/** Local JSON file (default when no cloud database is configured). Writes atomically. */
public class FilePersistence implements PersistenceBackend {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Path dataFile;

    public FilePersistence(Path dataFile) {
        this.dataFile = dataFile.toAbsolutePath();
    }

    @Override
    public synchronized ProjectService.PersistedData load() {
        if (!Files.exists(dataFile)) return null;
        try {
            return mapper.readValue(dataFile.toFile(), ProjectService.PersistedData.class);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo leer el archivo de proyectos", ex);
        }
    }

    @Override
    public synchronized void sync(Map<String, Project> projects, Map<String, Workspace> workspaces, String changedId) {
        try {
            Files.createDirectories(dataFile.getParent());
            ProjectService.PersistedData data = new ProjectService.PersistedData();
            data.projects = new HashMap<>(projects);
            data.workspaces = new HashMap<>(workspaces);
            Path temporary = Files.createTempFile(dataFile.getParent(), "nexo-", ".tmp");
            try {
                mapper.writeValue(temporary.toFile(), data);
                try {
                    Files.move(temporary, dataFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException ex) {
                    Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar el proyecto", ex);
        }
    }

    @Override
    public String describe() {
        return "file:" + dataFile.getFileName();
    }
}
