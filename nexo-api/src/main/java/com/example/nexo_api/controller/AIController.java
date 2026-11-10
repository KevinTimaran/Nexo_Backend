package com.example.nexo_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping({"/api/v1/ai", "/ai"})
public class AIController {

    @PostMapping("/reply")
    public ResponseEntity<Map<String, Object>> reply(@RequestBody Map<String, Object> body) {
        String query = body.containsKey("text") ? String.valueOf(body.get("text")) : "Consulta recibida";
        if (query.matches("(?is).*(data:image/|!\\[[^]]*]\\([^)]*\\.(png|jpe?g|gif|webp)\\)).*")) {
            return ResponseEntity.status(422).body(Map.of("error", "Nexo solo procesa texto; no se admiten imágenes"));
        }
        if (query.codePointCount(0, query.length()) > 20_000) {
            return ResponseEntity.status(422).body(Map.of("error", "El mensaje excede el límite de 20.000 caracteres"));
        }

        Map<String, Object> assistantMessage = new HashMap<>();
        assistantMessage.put("id", "msg-" + UUID.randomUUID().toString().substring(0, 8));
        assistantMessage.put("role", "assistant");
        assistantMessage.put("createdAt", Instant.now().toString());
        assistantMessage.put("cardType", "idea");
        Map<String, Object> generativeUi = buildGenerativeUi(query);
        if (generativeUi != null) {
            String type = String.valueOf(generativeUi.get("type"));
            assistantMessage.put("action", "chat");
            assistantMessage.put("readyForStructuring", false);
            assistantMessage.put("ui", generativeUi);
            assistantMessage.put("text", switch (type) {
                case "timeline" -> "Organicé el objetivo como una hoja de ruta editable. Puedes ajustar fechas, orden y avance sin dejar de conversar.";
                case "decision_comparator" -> "Preparé una comparación ponderada. Ajusta la importancia de cada criterio para ver cómo cambia la recomendación.";
                default -> "Convertí las propuestas en tarjetas accionables. Puedes editarlas y decidir cuáles deben pasar al Tablero de Conceptos.";
            });
        } else {
            assistantMessage.put("text", "He analizado tu propuesta sobre: \"" + query + "\". He organizado los componentes clave para tu lienzo.");
        }
        assistantMessage.put("simplifiedExplanation", "Resumen conceptual: Esta propuesta define un flujo directo de interacción y validación.");
        assistantMessage.put("showExplanation", false);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", assistantMessage);
        resp.put("newNodes", Collections.emptyList());
        resp.put("newRelations", Collections.emptyList());
        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> buildGenerativeUi(String query) {
        String text = query == null ? "" : query.toLowerCase(Locale.ROOT);

        if (text.matches(".*(línea de tiempo|linea de tiempo|hoja de ruta|roadmap|primeros? \\d+ días|planificar.{0,30}\\d+ días).*")) {
            LocalDate today = LocalDate.now();
            return ordered(
                    "type", "timeline",
                    "title", "Ruta de apertura · 90 días",
                    "description", "Cambia fechas, reorganiza las fases y marca el avance directamente.",
                    "items", List.of(
                            timelineItem("phase-1", "Validar ubicación y presupuesto", "Confirmar demanda, arriendo e inversión inicial.", today, today.plusDays(14), List.of(), true),
                            timelineItem("phase-2", "Permisos y proveedores", "Formalizar el negocio y negociar el inventario inicial.", today.plusDays(15), today.plusDays(34), List.of("phase-1"), false),
                            timelineItem("phase-3", "Adecuación e inventario", "Preparar el local, ventas y surtido prioritario.", today.plusDays(35), today.plusDays(59), List.of("phase-2"), false),
                            timelineItem("phase-4", "Preapertura y lanzamiento", "Probar la operación y ejecutar la campaña de apertura.", today.plusDays(60), today.plusDays(89), List.of("phase-3"), true)
                    )
            );
        }

        if (text.matches(".*(comparar|comparación|comparacion|cuál.{0,30}conviene|cual.{0,30}conviene|ubicaciones?).*")) {
            return ordered(
                    "type", "decision_comparator",
                    "title", "Comparación de ubicaciones",
                    "description", "Ajusta los pesos; la recomendación se recalcula al instante.",
                    "criteria", List.of(
                            ordered("id", "investment", "label", "Inversión", "weight", 8),
                            ordered("id", "demand", "label", "Demanda potencial", "weight", 10),
                            ordered("id", "access", "label", "Accesibilidad", "weight", 7),
                            ordered("id", "risk", "label", "Riesgo controlado", "weight", 6)
                    ),
                    "options", List.of(
                            decisionOption("downtown", "Centro comercial", "Alto flujo y arriendo elevado.", 4, 9, 9, 6),
                            decisionOption("residential", "Zona residencial", "Costos moderados y clientela recurrente.", 8, 7, 7, 8),
                            decisionOption("industrial", "Corredor industrial", "Demanda especializada y menor tráfico peatonal.", 7, 8, 6, 7)
                    )
            );
        }

        if (text.matches(".*(tarjetas?.{0,30}(ideas?|accionables?|conceptos?)|ideas?.{0,20}accionables?|propón.{0,20}ideas?|propon.{0,20}ideas?).*")) {
            return ordered(
                    "type", "idea_cards",
                    "title", "Ideas accionables para tu proyecto",
                    "description", "Solo las propuestas que envíes pasarán al Tablero de Conceptos.",
                    "items", List.of(
                            ideaCard("idea-advice", "Asesoría personalizada", "Recomendar materiales y cantidades según el trabajo de cada cliente.", List.of("Mayor confianza", "Ticket promedio más alto"), List.of("Capacitación técnica", "Catálogo estructurado"), List.of("Experiencia del cliente", "Ventas"), "technical", "high"),
                            ideaCard("idea-delivery", "Entrega local rápida", "Despachar pedidos urgentes con tiempos y tarifas claras.", List.of("Conveniencia", "Diferenciación local"), List.of("Vehículo o aliado", "Canal de pedidos"), List.of("Logística", "Modelo económico"), "economic", "medium"),
                            ideaCard("idea-kits", "Kits por proyecto", "Agrupar herramientas y consumibles en paquetes listos.", List.of("Compra más simple", "Inventario con propósito"), List.of("Análisis de demanda", "Empaque"), List.of("Inventario", "Marketing"), "economic", "medium")
                    )
            );
        }

        return null;
    }

    private Map<String, Object> timelineItem(String id, String title, String description, LocalDate start, LocalDate end, List<String> dependencies, boolean milestone) {
        return ordered("id", id, "title", title, "description", description, "startDate", start.toString(), "endDate", end.toString(), "dependencies", dependencies, "milestone", milestone, "status", "planned");
    }

    private Map<String, Object> decisionOption(String id, String title, String description, int investment, int demand, int access, int risk) {
        return ordered("id", id, "title", title, "description", description, "scores", ordered("investment", investment, "demand", demand, "access", access, "risk", risk));
    }

    private Map<String, Object> ideaCard(String id, String title, String description, List<String> benefits, List<String> resources, List<String> relations, String category, String priority) {
        return ordered("id", id, "title", title, "description", description, "benefits", benefits, "resources", resources, "relations", relations, "status", "pending", "category", category, "priority", priority, "phase", "planning");
    }

    private Map<String, Object> ordered(Object... values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            result.put(String.valueOf(values[index]), values[index + 1]);
        }
        return result;
    }

    @PostMapping("/explain")
    public ResponseEntity<Map<String, Object>> explain(@RequestBody Map<String, Object> body) {
        String conceptTitle = body.containsKey("conceptTitle") ? String.valueOf(body.get("conceptTitle")) : "este concepto";
        Map<String, Object> resp = new HashMap<>();
        resp.put("explanation", "En términos simples: " + conceptTitle + " representa un componente indispensable dentro de la arquitectura de la idea.");
        return ResponseEntity.ok(resp);
    }
}
