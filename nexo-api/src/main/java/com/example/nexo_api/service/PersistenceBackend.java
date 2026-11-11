package com.example.nexo_api.service;

import com.example.nexo_api.model.Project;
import com.example.nexo_api.model.Workspace;

import java.util.Map;

/**
 * Where projects and workspaces are stored. ProjectService keeps the working copy in memory and
 * calls the backend BEFORE changing memory, so a failed write never leaves the two out of sync.
 */
public interface PersistenceBackend {

    /** Everything stored, or null when nothing has ever been stored (first run). */
    ProjectService.PersistedData load();

    /**
     * Persists the given state. {@code changedId} names the single project that changed so
     * database backends can write one row; null means "write everything".
     * A project missing from {@code projects} is deleted.
     */
    void sync(Map<String, Project> projects, Map<String, Workspace> workspaces, String changedId);

    /** Called once after the first-run seed/import so that "empty" and "never initialised" differ. */
    default void initialize(Map<String, Project> projects, Map<String, Workspace> workspaces) {
        sync(projects, workspaces, null);
    }

    /** Human readable description for logs and /health. Never includes credentials. */
    String describe();
}
