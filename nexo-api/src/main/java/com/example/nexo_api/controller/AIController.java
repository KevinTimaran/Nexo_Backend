package com.example.nexo_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping({"/api/v1/ai", "/ai"})
public class AIController {

    @PostMapping("/reply")
    public ResponseEntity<Map<String, Object>> reply(@RequestBody Map<String, Object> body) {
        String query = body.containsKey("text") ? String.valueOf(body.get("text")) : "Consulta recibida";

        Map<String, Object> assistantMessage = new HashMap<>();
        assistantMessage.put("id", "msg-" + UUID.randomUUID().toString().substring(0, 8));
        assistantMessage.put("role", "assistant");
        assistantMessage.put("createdAt", Instant.now().toString());
        assistantMessage.put("cardType", "idea");
        assistantMessage.put("text", "He analizado tu propuesta sobre: \"" + query + "\". He organizado los componentes clave para tu lienzo.");
        assistantMessage.put("simplifiedExplanation", "Resumen conceptual: Esta propuesta define un flujo directo de interacción y validación.");
        assistantMessage.put("showExplanation", false);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", assistantMessage);
        resp.put("newNodes", Collections.emptyList());
        resp.put("newRelations", Collections.emptyList());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/explain")
    public ResponseEntity<Map<String, Object>> explain(@RequestBody Map<String, Object> body) {
        String conceptTitle = body.containsKey("conceptTitle") ? String.valueOf(body.get("conceptTitle")) : "este concepto";
        Map<String, Object> resp = new HashMap<>();
        resp.put("explanation", "En términos simples: " + conceptTitle + " representa un componente indispensable dentro de la arquitectura de la idea.");
        return ResponseEntity.ok(resp);
    }
}
