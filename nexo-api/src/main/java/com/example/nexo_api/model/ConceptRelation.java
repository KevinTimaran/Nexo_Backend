package com.example.nexo_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConceptRelation {
    private String id;
    private String from;
    private String to;
    private String semanticKey;
    private String origin;
    private String definitionVersion;
    private String type;
    private String label;
    private Double value;
    private String source;
    private String target;

    public ConceptRelation() {}

    public ConceptRelation(String id, String from, String to) {
        this.id = id;
        this.from = from;
        this.to = to;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
    public String getSemanticKey() { return semanticKey; }
    public void setSemanticKey(String semanticKey) { this.semanticKey = semanticKey; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDefinitionVersion() { return definitionVersion; }
    public void setDefinitionVersion(String definitionVersion) { this.definitionVersion = definitionVersion; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
}
