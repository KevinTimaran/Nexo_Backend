package com.example.nexo_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateWorkspaceRequest {
    private ConceptMap map;
    private List<ChatMessage> messages;

    public UpdateWorkspaceRequest() {}

    public ConceptMap getMap() {
        return map;
    }

    public void setMap(ConceptMap map) {
        this.map = map;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }
}
