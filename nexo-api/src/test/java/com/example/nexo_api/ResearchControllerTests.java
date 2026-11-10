package com.example.nexo_api;

import com.example.nexo_api.controller.ResearchController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResearchControllerTests {

    private ResearchController controller;

    @BeforeEach
    void setUp() {
        controller = new ResearchController();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testScenarioAHardwareStoreReturnsNationalStatsWithLocalWarning() {
        ResponseEntity<Map<String, Object>> response = controller.search(Map.of(
                "query", "Quiero abrir una ferretería en mi pueblo",
                "targetHypothesisId", "hypo-1"
        ));

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals("supports", body.get("validationState"));
        List<Map<String, Object>> sources = (List<Map<String, Object>>) body.get("sources");
        assertTrue(sources.size() >= 2);
        assertTrue(sources.stream().anyMatch(s -> s.get("organization").toString().contains("DANE")));

        List<Map<String, Object>> evidence = (List<Map<String, Object>>) body.get("evidenceRecords");
        assertTrue(evidence.stream().anyMatch(e -> e.containsKey("localContextWarning")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testScenarioBAcademicThesisReturnsMixedWithHighTrustAcademicSources() {
        ResponseEntity<Map<String, Object>> response = controller.search(Map.of(
                "query", "Deserción de estudiantes en ingeniería de software por programación difícil"
        ));

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals("mixed", body.get("validationState"));
        List<Map<String, Object>> sources = (List<Map<String, Object>>) body.get("sources");
        // Verify UNESCO and MinEdu have higher trustScore than commercial blog
        Map<String, Object> unesco = sources.stream().filter(s -> s.get("id").equals("src-unesco-stem")).findFirst().orElseThrow();
        Map<String, Object> blog = sources.stream().filter(s -> s.get("id").equals("src-commercial-blog")).findFirst().orElseThrow();

        int unescoScore = (int) unesco.get("trustScore");
        int blogScore = (int) blog.get("trustScore");
        assertTrue(unescoScore > blogScore, "Academic source trust score must exceed commercial blog");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testScenarioCSoftwareAccessibilityAndPrivacy() {
        ResponseEntity<Map<String, Object>> response = controller.search(Map.of(
                "query", "Requisitos de accesibilidad WCAG y ley 1581 de privacidad para mi app"
        ));

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals("supports", body.get("validationState"));
        List<Map<String, Object>> sources = (List<Map<String, Object>>) body.get("sources");
        assertTrue(sources.stream().anyMatch(s -> s.get("url").toString().contains("w3.org")));
        assertTrue(sources.stream().anyMatch(s -> s.get("url").toString().contains("sic.gov.co")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNoEvidenceScenarioReturnsHonestInsufficientEvidence() {
        ResponseEntity<Map<String, Object>> response = controller.search(Map.of(
                "query", "Los carpinteros de San Juan de Nepomuceno no tienen taladros de impacto"
        ));

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals("insufficient_evidence", body.get("validationState"));
        List<?> sources = (List<?>) body.get("sources");
        assertEquals(0, sources.size());

        List<String> steps = (List<String>) body.get("suggestedEmpiricalSteps");
        assertNotNull(steps);
        assertTrue(steps.size() >= 3);
        assertTrue(steps.stream().anyMatch(s -> s.toLowerCase().contains("entrevista")));
    }

    @Test
    void testRejectsEmptyQuery() {
        ResponseEntity<Map<String, Object>> response = controller.search(Map.of("query", "   "));
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testRejectsImagePayload() {
        ResponseEntity<Map<String, Object>> response = controller.search(Map.of(
                "query", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="
        ));
        assertEquals(422, response.getStatusCode().value());
    }
}
