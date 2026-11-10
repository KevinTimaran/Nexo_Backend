package com.example.nexo_api;

import com.example.nexo_api.model.*;
import com.example.nexo_api.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class WorkspacePersistenceTests {
    @TempDir Path directory;

    @Test void revisionIdempotencyAndRestart() {
        Path file = directory.resolve("nexo.json");
        ProjectService first = new ProjectService(file);
        first.initSeedData();
        NewProjectRequest input = new NewProjectRequest();
        input.setName("Ferretería local");
        Project project = first.createProject(input);
        UpdateWorkspaceRequest update = new UpdateWorkspaceRequest();
        update.setExpectedRevision(0L);
        update.setOperationId("operation-1");
        update.setMap(new ConceptMap(new ArrayList<>(), new ArrayList<>()));
        ChatMessage user = new ChatMessage("user-1", "user", "2026-11-09T00:00:00Z", "Vender materiales a vecinos");
        user.setEventId("event-1");
        ChatMessage assistant = new ChatMessage("assistant-1", "assistant", "2026-11-09T00:00:01Z", "Comparemos opciones");
        assistant.setReplyTo("user-1");
        assistant.setUi(Map.of("type", "decision_comparator", "selectedOptionId", "site-2"));
        assistant.setDimensions(List.of(Map.of("id", "economic", "label", "Económica")));
        update.setMessages(List.of(user, assistant));
        update.setDiscovery(Map.of("lifecycle", "generated", "generatedDefinitionVersion", "v-1", "tombstones", List.of("deleted-node")));
        Workspace saved = first.saveWorkspace(project.getId(), update);
        assertEquals(1, saved.getRevision());
        assertEquals(1, first.saveWorkspace(project.getId(), update).getRevision());
        UpdateWorkspaceRequest stale = new UpdateWorkspaceRequest();
        stale.setExpectedRevision(0L);
        stale.setOperationId("operation-2");
        assertThrows(ProjectService.RevisionConflictException.class, () -> first.saveWorkspace(project.getId(), stale));

        ProjectService restarted = new ProjectService(file);
        restarted.initSeedData();
        Workspace loaded = restarted.getWorkspace(project.getId());
        assertNotNull(loaded);
        assertEquals(1, loaded.getRevision());
        assertEquals("generated", loaded.getDiscovery().get("lifecycle"));
        assertEquals("Vender materiales a vecinos", loaded.getMessages().get(0).getText());
        assertEquals("site-2", loaded.getMessages().get(1).getUi().get("selectedOptionId"));
        assertEquals("economic", loaded.getMessages().get(1).getDimensions().get(0).get("id"));
        assertEquals(1, restarted.saveWorkspace(project.getId(), update).getRevision());
    }
}
