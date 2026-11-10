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
    private String semanticKey;
    private String origin;
    private String verification;
    private List<String> sourceFactIds;
    private String definitionVersion;
    private List<String> manuallyEditedFields;
    private String name;
    private String desc;
    private Double z;
    private Double val;
    private Double importance;
    private Double relevanceScore;
    private String group;

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
    public String getSemanticKey() { return semanticKey; }
    public void setSemanticKey(String semanticKey) { this.semanticKey = semanticKey; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getVerification() { return verification; }
    public void setVerification(String verification) { this.verification = verification; }
    public List<String> getSourceFactIds() { return sourceFactIds; }
    public void setSourceFactIds(List<String> sourceFactIds) { this.sourceFactIds = sourceFactIds; }
    public String getDefinitionVersion() { return definitionVersion; }
    public void setDefinitionVersion(String definitionVersion) { this.definitionVersion = definitionVersion; }
    public List<String> getManuallyEditedFields() { return manuallyEditedFields; }
    public void setManuallyEditedFields(List<String> manuallyEditedFields) { this.manuallyEditedFields = manuallyEditedFields; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public Double getZ() { return z; }
    public void setZ(Double z) { this.z = z; }
    public Double getVal() { return val; }
    public void setVal(Double val) { this.val = val; }
    public Double getImportance() { return importance; }
    public void setImportance(Double importance) { this.importance = importance; }
    public Double getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(Double relevanceScore) { this.relevanceScore = relevanceScore; }
    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }
}
