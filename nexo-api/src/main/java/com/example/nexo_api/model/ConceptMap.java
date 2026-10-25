package com.example.nexo_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConceptMap {
    private List<ConceptNode> nodes = new ArrayList<>();
    private List<ConceptRelation> relations = new ArrayList<>();

    public ConceptMap() {}

    public ConceptMap(List<ConceptNode> nodes, List<ConceptRelation> relations) {
        this.nodes = nodes != null ? nodes : new ArrayList<>();
        this.relations = relations != null ? relations : new ArrayList<>();
    }

    public List<ConceptNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<ConceptNode> nodes) {
        this.nodes = nodes != null ? nodes : new ArrayList<>();
    }

    public List<ConceptRelation> getRelations() {
        return relations;
    }

    public void setRelations(List<ConceptRelation> relations) {
        this.relations = relations != null ? relations : new ArrayList<>();
    }
}
