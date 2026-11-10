package com.example.nexo_api;

import com.example.nexo_api.controller.HealthController;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthControllerTests {

    @Test
    @SuppressWarnings("unchecked")
    void reportsNeo4jAsNotConfiguredWhileApplicationDoesNotUseIt() {
        Map<String, Object> body = new HealthController().getHealth().getBody();
        Map<String, Object> components = (Map<String, Object>) body.get("components");
        Map<String, Object> neo4j = (Map<String, Object>) components.get("neo4j");

        assertEquals("NOT_CONFIGURED", neo4j.get("status"));
        assertEquals("UP", body.get("status"));
    }
}
