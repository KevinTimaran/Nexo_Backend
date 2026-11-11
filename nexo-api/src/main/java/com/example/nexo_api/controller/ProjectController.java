package com.example.nexo_api.controller;

import com.example.nexo_api.config.AuthUser;
import com.example.nexo_api.model.*;
import com.example.nexo_api.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/** Every endpoint works on the caller's own projects only; other users' ids answer 404. */
@RestController
@RequestMapping({"/api/v1/projects", "/projects"})
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<Project>> listProjects(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(projectService.listProjects(AuthUser.id(jwt), lang, status, search));
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@AuthenticationPrincipal Jwt jwt, @RequestBody NewProjectRequest request) {
        Project created = projectService.createProject(AuthUser.id(jwt), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        Project p = projectService.getProject(AuthUser.id(jwt), id);
        if (p == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(p);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Project> updateProject(@AuthenticationPrincipal Jwt jwt, @PathVariable String id, @RequestBody Map<String, Object> updates) {
        Project updated = projectService.updateProject(AuthUser.id(jwt), id, updates);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(updated);
    }

    /** Idempotent: deleting a project that no longer exists is a success (204). Storage failures return 503. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        try {
            projectService.deleteProject(AuthUser.id(jwt), id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException storageFailure) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "No se pudo eliminar el proyecto: el almacenamiento no respondió. Inténtalo de nuevo."));
        }
    }

    @GetMapping("/{id}/workspace")
    public ResponseEntity<Workspace> getWorkspace(@AuthenticationPrincipal Jwt jwt, @PathVariable String id, @RequestParam(required = false) String lang) {
        Workspace ws = projectService.getWorkspace(AuthUser.id(jwt), id);
        if (ws == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(ws);
    }

    @PutMapping("/{id}/workspace")
    public ResponseEntity<?> saveWorkspace(@AuthenticationPrincipal Jwt jwt, @PathVariable String id, @RequestBody UpdateWorkspaceRequest request) {
        try {
            Workspace saved = projectService.saveWorkspace(AuthUser.id(jwt), id, request);
            return saved == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(saved);
        } catch (ProjectService.RevisionConflictException conflict) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(conflict.getCurrent());
        } catch (IllegalArgumentException invalid) {
            return ResponseEntity.badRequest().body(Map.of("error", invalid.getMessage()));
        }
    }

    @PostMapping("/{id}/documents/generate")
    public ResponseEntity<?> generateDocument(@AuthenticationPrincipal Jwt jwt, @PathVariable String id, @RequestBody Map<String, Object> request) {
        Project p = projectService.getProject(AuthUser.id(jwt), id);
        if (p == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", "doc-" + UUID.randomUUID().toString().substring(0, 8));
        doc.put("projectId", id);
        doc.put("title", "Informe Ejecutivo — " + p.getName());
        doc.put("createdAt", Instant.now().toString());
        return ResponseEntity.ok(doc);
    }
}
