package com.example.nexo_api.controller;

import com.example.nexo_api.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1", ""})
public class HealthController {

    @Autowired(required = false)
    private ProjectService projects;

    @GetMapping({"/health", "/status"})
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "UP");
        body.put("connected", true);
        body.put("service", "nexo-api");
        body.put("version", "1.0.0");
        body.put("timestamp", System.currentTimeMillis());

        Map<String, Object> components = new HashMap<>();
        Map<String, Object> neo4j = new HashMap<>();
        // The application currently persists workspaces as JSON and does not use Neo4j.
        // Keep the health contract honest until a real integration is configured and probed.
        neo4j.put("status", "NOT_CONFIGURED");
        components.put("neo4j", neo4j);
        if (projects != null) {
            Map<String, Object> storage = new HashMap<>();
            storage.put("backend", projects.storageDescription());
            components.put("storage", storage);
        }
        body.put("components", components);

        return ResponseEntity.ok(body);
    }
}
