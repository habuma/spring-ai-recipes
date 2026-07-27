package com.example.graphworkflowloop;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DiagramBuilder {

  @Value("${diagram.output.path:./diagrams}")
  private String outputPath;

  public void createMermaidDiagram(CompiledGraph compiledGraph) {
    createDiagram(compiledGraph, GraphRepresentation.Type.MERMAID);
  }

  public void createPlantUMLDiagram(CompiledGraph compiledGraph) {
    createDiagram(compiledGraph, GraphRepresentation.Type.PLANTUML);
  }

  private void createDiagram(CompiledGraph compiledGraph, GraphRepresentation.Type type) {
    GraphRepresentation graph = compiledGraph.getGraph(type);
    String graphContent = graph.content();
    String fileName = "/support-triage" +
        (type == GraphRepresentation.Type.MERMAID ? ".mmd" : ".puml");
    FileUtils.writeCodeToFile(outputPath, fileName, graphContent);
  }

}
