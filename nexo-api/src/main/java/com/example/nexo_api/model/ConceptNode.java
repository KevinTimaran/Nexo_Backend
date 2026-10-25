package com.example.nexo_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConceptNode {
    private String id;
    private String kind;      // "concept", "viability", "risk"
    private String category;  // "legal", "economic", "political", "technical"
    private String priority;  // "high", "medium", "low"
    private String title;
    private String description;
    private String metadata;
    private String status;    // "confirmed", "exploring", "review"
    private double x;
    private double y;
    private String phase;     // "planning", "execution", "validation"
    private String details;
    private List<String> tags;

    public ConceptNode() {}

    public ConceptNode(String id, String kind, String title, String description, String metadata, String status, double x, double y, String phase) {
        this.id = id;
        this.kind = kind;
        this.title = title;
        this.description = description;
        this.metadata = metadata;
        this.status = status;
        this.x = x;
        this.y = y;
        this.phase = phase;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
