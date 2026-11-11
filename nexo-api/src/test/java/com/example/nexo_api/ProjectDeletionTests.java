package com.example.nexo_api;

import com.example.nexo_api.controller.ProjectController;
import com.example.nexo_api.model.NewProjectRequest;
import com.example.nexo_api.model.Project;
import com.example.nexo_api.model.Workspace;
import com.example.nexo_api.service.FilePersistence;
import com.example.nexo_api.service.PersistenceBackend;
import com.example.nexo_api.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProjectDeletionTests {
    private static final String USER = "user-a";
    @TempDir Path directory;

    /** Storage that can be told to fail, to prove a failed write never corrupts memory. */
    static class FlakyBackend implements PersistenceBackend {
        private final PersistenceBackend delegate;
        boolean failWrites;
        FlakyBackend(PersistenceBackend delegate) { this.delegate = delegate; }
        @Override public ProjectService.PersistedData load() { return delegate.load(); }
        @Override public void sync(Map<String, Project> projects, Map<String, Workspace> workspaces, String changedId) {
            if (failWrites) throw new IllegalStateException("No se pudo guardar el proyecto");
            delegate.sync(projects, workspaces, changedId);
        }
        @Override public String describe() { return "flaky"; }
    }

    private Project newProject(ProjectService service, String name) {
        NewProjectRequest request = new NewProjectRequest();
        request.setName(name);
        return service.createProject(USER, request);
    }

    @Test void deletedProjectStaysDeletedAfterRestart() {
        Path file = directory.resolve("data.json");
        ProjectService first = new ProjectService(file);
        first.init();
        Project gone = newProject(first, "Para borrar");
        Project kept = newProject(first, "Para conservar");
        assertTrue(first.deleteProject(USER, gone.getId()));

        ProjectService restarted = new ProjectService(file);
        restarted.init();
        assertNull(restarted.getProject(USER, gone.getId()));
        assertNotNull(restarted.getProject(USER, kept.getId()));
    }

    @Test void deletingTwiceIsNotAnError() {
        ProjectService service = new ProjectService(directory.resolve("data.json"));
        service.init();
        Project mine = newProject(service, "Una vez");
        assertTrue(service.deleteProject(USER, mine.getId()));
        assertFalse(service.deleteProject(USER, mine.getId()), "Already gone: reported, not thrown");
        ProjectController controller = new ProjectController(service);
        assertEquals(HttpStatus.NO_CONTENT, controller.deleteProject(null, mine.getId()).getStatusCode(), "Idempotent delete answers 204");
        assertEquals(HttpStatus.NO_CONTENT, controller.deleteProject(null, "never-existed").getStatusCode());
    }

    @Test void failedWriteLeavesTheProjectIntactAndReportsUnavailable() {
        FlakyBackend backend = new FlakyBackend(new FilePersistence(directory.resolve("data.json")));
        ProjectService service = new ProjectService(backend, null);
        service.init();
        Project mine = newProject(service, "Debe sobrevivir");

        backend.failWrites = true;
        assertThrows(IllegalStateException.class, () -> service.deleteProject(USER, mine.getId()));
        assertNotNull(service.getProject(USER, mine.getId()), "A failed delete must not remove the project from memory");
        assertNotNull(service.getWorkspace(USER, mine.getId()));

        backend.failWrites = false;
        assertTrue(service.deleteProject(USER, mine.getId()), "A retry after storage recovers succeeds");
        assertNull(service.getProject(USER, mine.getId()));
    }

    @Test void failedCreateOrRenameDoesNotLeaveGhostProjects() {
        FlakyBackend backend = new FlakyBackend(new FilePersistence(directory.resolve("data.json")));
        ProjectService service = new ProjectService(backend, null);
        service.init();
        Project mine = newProject(service, "Original");
        int before = service.listProjects(USER, null, null, null).size();

        backend.failWrites = true;
        assertThrows(IllegalStateException.class, () -> newProject(service, "Fantasma"));
        assertEquals(before, service.listProjects(USER, null, null, null).size());
        assertThrows(IllegalStateException.class, () -> service.updateProject(USER, mine.getId(), Map.of("name", "Cambiado")));
        assertEquals("Original", service.getProject(USER, mine.getId()).getName());
    }
}
