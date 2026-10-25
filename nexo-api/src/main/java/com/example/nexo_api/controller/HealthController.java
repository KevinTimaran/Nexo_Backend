package com.example.nexo_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping({"/api/v1", ""})
public class HealthController {

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
        neo4j.put("status", "UP");
        components.put("neo4j", neo4j);
        body.put("components", components);

        return ResponseEntity.ok(body);
    }
}
