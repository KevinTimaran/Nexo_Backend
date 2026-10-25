package com.example.nexo_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Workspace {
    private Project project;
    private ConceptMap map = new ConceptMap();
    private List<ChatMessage> messages = new ArrayList<>();

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
}
