package com.example.nexo_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Workspace {
    private Project project;
    private ConceptMap map = new ConceptMap();
    private List<ChatMessage> messages = new ArrayList<>();
    private Map<String, Object> discovery;
    private long revision;
    private Map<String, Long> appliedOperations = new java.util.LinkedHashMap<>();

    public Workspace() {}

    public Workspace(Project project, ConceptMap map, List<ChatMessage> messages) {
        this.project = project;
        this.map = map != null ? map : new ConceptMap();
        this.messages = messages != null ? messages : new ArrayList<>();
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ConceptMap getMap() {
        return map;
    }

    public void setMap(ConceptMap map) {
        this.map = map != null ? map : new ConceptMap();
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
    }

    public Map<String, Object> getDiscovery() { return discovery; }
    public void setDiscovery(Map<String, Object> discovery) { this.discovery = discovery; }
    public long getRevision() { return revision; }
    public void setRevision(long revision) { this.revision = revision; }
    public Map<String, Long> getAppliedOperations() { return appliedOperations; }
    public void setAppliedOperations(Map<String, Long> appliedOperations) { this.appliedOperations = appliedOperations != null ? appliedOperations : new java.util.LinkedHashMap<>(); }
}
