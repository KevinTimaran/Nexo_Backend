package com.example.nexo_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConceptRelation {
    private String id;
    private String from;
    private String to;

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
}
