package com.example.nexo_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

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
}
