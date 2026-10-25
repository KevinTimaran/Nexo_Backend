package com.example.nexo_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Project {
    private String id;
    private String name;
    private String description;
    private String status; // "active", "draft", "archived"
    private String icon;   // "lightbulb", "leaf", "scale", "rocket", "book", "compass"
    private String updatedAt;
    private int nodeCount;

    public Project() {}

    public Project(String id, String name, String description, String status, String icon, String updatedAt, int nodeCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.icon = icon;
        this.updatedAt = updatedAt;
        this.nodeCount = nodeCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }
}
