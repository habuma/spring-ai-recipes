package com.example.simplegraphrag;

import java.util.List;

public record KnowledgeGraph(List<Edge> edges) {

  public void addAll(List<Edge> edges) {
    this.edges.addAll(edges);
  }

}
