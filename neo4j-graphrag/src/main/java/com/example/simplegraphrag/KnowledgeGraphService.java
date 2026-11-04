package com.example.simplegraphrag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class KnowledgeGraphService {

  private static final Logger LOGGER = LoggerFactory.getLogger(KnowledgeGraphService.class);

  private final ChatClient chatClient;
  private final Neo4jClient neo4jClient;


  public KnowledgeGraphService(ChatModel chatModel, Neo4jClient neo4jClient) {
    var chatClientBuilder = ChatClient.builder(chatModel);
    this.chatClient = chatClientBuilder
        .defaultSystem("""
            Extract DESIGNED_BY and PUBLISHED_BY relationships from the given text.
            Only return facts supported by the text.
            """)
        .build();
    this.neo4jClient = neo4jClient;
  }

  public void addToKnowledgeGraph(String text) {
    var extracted = chatClient.prompt()
        .user(u -> u.text("""
            Text:
            {text}
            """)
            .param("text", text))
        .call()
        .entity(KnowledgeGraph.class);

    for (Edge edge : extracted.edges()) {
      neo4jClient.query("""
                MERGE (source:Entity {name: $from})
                MERGE (target:Entity {name: $to})
                MERGE (source)-[:$($relation)]->(target)
                """)
          .bind(edge.from()).to("from")
          .bind(edge.to()).to("to")
          .bind(edge.relation().name()).to("relation")
          .run();
    }
  }

  @Tool(description = """
        Find relationships in a board game knowledge graph.
        Supply at least one of 'from' or 'to'.
        """)
  public List<Edge> findRelationships(
      @ToolParam(required = false) String from,
      Relation relation,
      @ToolParam(required = false) String to) {

    if (from == null && to == null) {
      return List.of();
    }

    LOGGER.info("Finding relationships in the knowledge graph. {} -- {} --> {}", from, relation, to);

    return neo4jClient.query("""
        MATCH (a:Entity)-[r:$($relation)]->(b:Entity)
        WHERE ($from = '' OR toLower(a.name) = toLower($from))
          AND ($to = '' OR toLower(b.name) = toLower($to))
        RETURN a.name AS source,
               type(r) AS relation,
               b.name AS target
        """)
        .bindAll(Map.of(
            "relation", relation.name(),
            "from", from == null ? "" : from,
            "to", to == null ? "" : to
        ))
        .fetch()
        .all()
        .stream()
        .map(row -> new Edge(
            (String) row.get("source"),
            Relation.valueOf((String) row.get("relation")),
            (String) row.get("target")
        ))
        .toList();
  }

}
