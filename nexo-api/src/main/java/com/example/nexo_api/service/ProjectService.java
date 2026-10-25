package com.example.nexo_api.service;

import com.example.nexo_api.model.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProjectService {

    private final Map<String, Project> projects = new ConcurrentHashMap<>();
    private final Map<String, Workspace> workspaces = new ConcurrentHashMap<>();

    @PostConstruct
    public void initSeedData() {
        // Seed Project 1: Campus Market
        Project campusMarket = new Project(
                "campus-market",
                "Mercado circular universitario",
                "Una red de reventa confiable donde estudiantes compran y venden artículos usados en el campus.",
                "active",
                "leaf",
                Instant.now().toString(),
                3
        );
        projects.put(campusMarket.getId(), campusMarket);

        List<ConceptNode> cmNodes = new ArrayList<>();
        ConceptNode n1 = new ConceptNode("cm-core", "concept", "Reventa entre estudiantes",
                "Estudiantes intercambian libros y muebles en una red verificada.",
                "Idea central", "confirmed", 60, 250, "planning");
        n1.setDetails("### Hipótesis de Partida\nLos estudiantes prefieren comprar localmente a compañeros verificados.");
        n1.setTags(List.of("core", "estudiantes", "reventa"));

        ConceptNode n2 = new ConceptNode("cm-demand", "viability", "Alta demanda al inicio de semestre",
                "68% de 212 estudiantes encuestados compró usados.",
                "Puntaje 74/100", "confirmed", 400, 250, "execution");
        n2.setDetails("### Validación Cuantitativa\nEncuesta aplicada en 4 facultades principales durante semana de inducción.");
        n2.setTags(List.of("demanda", "metricas"));

        ConceptNode n3 = new ConceptNode("cm-payments", "risk", "Pagos y protección al consumidor",
                "Gestionar pagos requiere proveedor autorizado y custodia temporal de fondos.",
                "Revisión legal", "review", 400, 480, "validation");
        n3.setDetails("### Marco Regulatorio\nIntegración pasarela segura con retención hasta confirmación física.");
        n3.setTags(List.of("legal", "pagos", "seguridad"));

        cmNodes.add(n1);
        cmNodes.add(n2);
        cmNodes.add(n3);

        List<ConceptRelation> cmRelations = new ArrayList<>();
        cmRelations.add(new ConceptRelation("cm-core->cm-demand", "cm-core", "cm-demand"));
        cmRelations.add(new ConceptRelation("cm-core->cm-payments", "cm-core", "cm-payments"));

        List<ChatMessage> cmMessages = new ArrayList<>();
        cmMessages.add(new ChatMessage("msg-001", "user", Instant.now().minusSeconds(300).toString(),
                "Quiero crear un mercado para que estudiantes revendan libros en el campus."));

        ChatMessage cmA2 = new ChatMessage("msg-002", "assistant", Instant.now().minusSeconds(290).toString(),
                "Estructuré tu idea en tres piezas fundamentales: idea central, validación de demanda y riesgos de pago.");
        cmA2.setReplyTo("msg-001");
        cmA2.setCardType("idea");
        cmA2.setSimplifiedExplanation("En términos sencillos: Diseñamos la idea central, cómo validar la demanda y qué riesgos legales debes vigilar.");
        cmA2.setShowExplanation(false);
        cmMessages.add(cmA2);

        workspaces.put(campusMarket.getId(), new Workspace(campusMarket, new ConceptMap(cmNodes, cmRelations), cmMessages));

        // Seed Project 2: Legal Aid
        Project legalAid = new Project(
                "legal-aid",
                "Asistente de orientación legal",
                "Orientación en lenguaje sencillo para entender tus derechos antes de acudir a un abogado.",
                "active",
                "scale",
                Instant.now().toString(),
                2
        );
        projects.put(legalAid.getId(), legalAid);

        List<ConceptNode> laNodes = new ArrayList<>();
        laNodes.add(new ConceptNode("la-core", "concept", "Triage legal preliminar",
                "Asistente que clasifica consultas legales comunes.",
                "Núcleo", "confirmed", 80, 200, "planning"));
        laNodes.add(new ConceptNode("la-risk", "risk", "Límite de responsabilidad",
                "Deslindar asesoría legal formal vinculante.",
                "Riesgo crítico", "confirmed", 420, 200, "validation"));

        List<ConceptRelation> laRelations = new ArrayList<>();
        laRelations.add(new ConceptRelation("la-core->la-risk", "la-core", "la-risk"));

        List<ChatMessage> laMessages = new ArrayList<>();
        laMessages.add(new ChatMessage("msg-la-01", "user", Instant.now().minusSeconds(600).toString(),
                "¿Cómo ayudar a personas sin recursos a entender términos legales?"));

        workspaces.put(legalAid.getId(), new Workspace(legalAid, new ConceptMap(laNodes, laRelations), laMessages));
    }

    public List<Project> listProjects(String lang, String status, String search) {
        List<Project> list = new ArrayList<>(projects.values());
        if (status != null && !status.isBlank() && !"all".equalsIgnoreCase(status)) {
            list.removeIf(p -> !status.equalsIgnoreCase(p.getStatus()));
        }
        if (search != null && !search.isBlank()) {
            String lower = search.toLowerCase();
            list.removeIf(p -> (p.getName() == null || !p.getName().toLowerCase().contains(lower)) &&
                               (p.getDescription() == null || !p.getDescription().toLowerCase().contains(lower)));
        }
        // Order by updatedAt desc
        list.sort((a, b) -> {
            if (a.getUpdatedAt() == null) return 1;
            if (b.getUpdatedAt() == null) return -1;
            return b.getUpdatedAt().compareTo(a.getUpdatedAt());
        });
        return list;
    }

    public Project createProject(NewProjectRequest request) {
        String id = "p-" + UUID.randomUUID().toString().substring(0, 8);
        String name = request.getName() != null && !request.getName().isBlank() ? request.getName().trim() : "Nuevo Proyecto";
        String description = request.getDescription() != null ? request.getDescription().trim() : "";
        String icon = request.getIcon() != null && !request.getIcon().isBlank() ? request.getIcon() : "lightbulb";
        String now = Instant.now().toString();

        Project project = new Project(id, name, description, "draft", icon, now, 0);
        projects.put(id, project);

        // Initialize empty workspace
        Workspace workspace = new Workspace(project, new ConceptMap(new ArrayList<>(), new ArrayList<>()), new ArrayList<>());
        workspaces.put(id, workspace);

        return project;
    }

    public Project getProject(String id) {
        return projects.get(id);
    }

    public Project updateProject(String id, Map<String, Object> updates) {
        Project p = projects.get(id);
        if (p == null) return null;

        if (updates.containsKey("name") && updates.get("name") instanceof String s) p.setName(s);
        if (updates.containsKey("description") && updates.get("description") instanceof String s) p.setDescription(s);
        if (updates.containsKey("status") && updates.get("status") instanceof String s) p.setStatus(s);
        if (updates.containsKey("icon") && updates.get("icon") instanceof String s) p.setIcon(s);

        p.setUpdatedAt(Instant.now().toString());
        return p;
    }

    public boolean deleteProject(String id) {
        Project removed = projects.remove(id);
        workspaces.remove(id);
        return removed != null;
    }

    public Workspace getWorkspace(String projectId) {
        Project p = projects.get(projectId);
        if (p == null) return null;

        Workspace ws = workspaces.get(projectId);
        if (ws == null) {
            ws = new Workspace(p, new ConceptMap(new ArrayList<>(), new ArrayList<>()), new ArrayList<>());
            workspaces.put(projectId, ws);
        }
        return ws;
    }

    public void saveWorkspace(String projectId, ConceptMap map, List<ChatMessage> messages) {
        Project p = projects.get(projectId);
        if (p == null) return;

        Workspace ws = workspaces.get(projectId);
        if (ws == null) {
            ws = new Workspace(p, map != null ? map : new ConceptMap(), messages != null ? messages : new ArrayList<>());
            workspaces.put(projectId, ws);
        } else {
            if (map != null) ws.setMap(map);
            if (messages != null) ws.setMessages(messages);
        }

        int nodeCount = ws.getMap() != null && ws.getMap().getNodes() != null ? ws.getMap().getNodes().size() : 0;
        p.setNodeCount(nodeCount);
        p.setUpdatedAt(Instant.now().toString());
    }
}
