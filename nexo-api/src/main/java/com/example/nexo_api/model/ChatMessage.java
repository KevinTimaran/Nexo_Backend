package com.example.nexo_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessage {
    private String id;
    private String role; // "user", "assistant", "system"
    private String createdAt;
    private String text;
    private String replyTo;
    private String cardType;
    private String simplifiedExplanation;
    private Boolean showExplanation;
    private Map<String, Object> insight;
    private Map<String, Object> actionableNode;
    private Map<String, Object> ui;
    private String eventId;
    private String requestId;
    private String questionnaireId;
    private Map<String, Object> answers;
    private String action;
    private Boolean readyForStructuring;
    private List<Map<String, Object>> discoveryQuestions;
    private List<Map<String, Object>> dimensions;
    private String chatPhase;

    public ChatMessage() {}

    public ChatMessage(String id, String role, String createdAt, String text) {
        this.id = id;
        this.role = role;
        this.createdAt = createdAt;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getSimplifiedExplanation() {
        return simplifiedExplanation;
    }

    public void setSimplifiedExplanation(String simplifiedExplanation) {
        this.simplifiedExplanation = simplifiedExplanation;
    }

    public Boolean getShowExplanation() {
        return showExplanation;
    }

    public void setShowExplanation(Boolean showExplanation) {
        this.showExplanation = showExplanation;
    }

    public Map<String, Object> getInsight() {
        return insight;
    }

    public void setInsight(Map<String, Object> insight) {
        this.insight = insight;
    }

    public Map<String, Object> getActionableNode() {
        return actionableNode;
    }

    public void setActionableNode(Map<String, Object> actionableNode) {
        this.actionableNode = actionableNode;
    }

    public Map<String, Object> getUi() {
        return ui;
    }

    public void setUi(Map<String, Object> ui) {
        this.ui = ui;
    }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getQuestionnaireId() { return questionnaireId; }
    public void setQuestionnaireId(String questionnaireId) { this.questionnaireId = questionnaireId; }
    public Map<String, Object> getAnswers() { return answers; }
    public void setAnswers(Map<String, Object> answers) { this.answers = answers; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Boolean getReadyForStructuring() { return readyForStructuring; }
    public void setReadyForStructuring(Boolean readyForStructuring) { this.readyForStructuring = readyForStructuring; }
    public List<Map<String, Object>> getDiscoveryQuestions() { return discoveryQuestions; }
    public void setDiscoveryQuestions(List<Map<String, Object>> discoveryQuestions) { this.discoveryQuestions = discoveryQuestions; }
    public List<Map<String, Object>> getDimensions() { return dimensions; }
    public void setDimensions(List<Map<String, Object>> dimensions) { this.dimensions = dimensions; }
    public String getChatPhase() { return chatPhase; }
    public void setChatPhase(String chatPhase) { this.chatPhase = chatPhase; }
}
