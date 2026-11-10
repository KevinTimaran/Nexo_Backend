package com.example.nexo_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateWorkspaceRequest {
    private ConceptMap map;
    private List<ChatMessage> messages;
    private Map<String, Object> discovery;
    private Long expectedRevision;
    private String operationId;

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
    public Map<String, Object> getDiscovery() { return discovery; }
    public void setDiscovery(Map<String, Object> discovery) { this.discovery = discovery; }
    public Long getExpectedRevision() { return expectedRevision; }
    public void setExpectedRevision(Long expectedRevision) { this.expectedRevision = expectedRevision; }
    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }
}
