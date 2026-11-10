package com.example.nexo_api.controller;

import com.example.nexo_api.model.*;
import com.example.nexo_api.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping({"/api/v1/projects", "/projects"})
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<Project>> listProjects(
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        List<Project> list = projectService.listProjects(lang, status, search);
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody NewProjectRequest request) {
        Project created = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable String id) {
        Project p = projectService.getProject(id);
        if (p == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(p);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        Project updated = projectService.updateProject(id, updates);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable String id) {
        boolean deleted = projectService.deleteProject(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/workspace")
    public ResponseEntity<Workspace> getWorkspace(@PathVariable String id, @RequestParam(required = false) String lang) {
        Workspace ws = projectService.getWorkspace(id);
        if (ws == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(ws);
    }

    @PutMapping("/{id}/workspace")
    public ResponseEntity<?> saveWorkspace(@PathVariable String id, @RequestBody UpdateWorkspaceRequest request) {
        try {
            Workspace saved = projectService.saveWorkspace(id, request);
            return saved == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(saved);
        } catch (ProjectService.RevisionConflictException conflict) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(conflict.getCurrent());
        } catch (IllegalArgumentException invalid) {
            return ResponseEntity.badRequest().body(Map.of("error", invalid.getMessage()));
        }
    }

    @PostMapping("/{id}/documents/generate")
    public ResponseEntity<Map<String, Object>> generateDocument(@PathVariable String id, @RequestBody Map<String, Object> request) {
        Project p = projectService.getProject(id);
        String projectName = p != null ? p.getName() : "Proyecto";
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", "doc-" + UUID.randomUUID().toString().substring(0, 8));
        doc.put("projectId", id);
        doc.put("title", "Informe Ejecutivo — " + projectName);
        doc.put("createdAt", Instant.now().toString());
        return ResponseEntity.ok(doc);
    }
}
