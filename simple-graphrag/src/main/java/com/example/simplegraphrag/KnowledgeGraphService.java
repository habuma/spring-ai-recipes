package com.example.simplegraphrag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KnowledgeGraphService {

  private static final Logger LOGGER = LoggerFactory.getLogger(KnowledgeGraphService.class);

  private final ChatClient chatClient;
  private final KnowledgeGraph knowledgeGraph;

  public KnowledgeGraphService(ChatModel chatModel) {
    var chatClientBuilder = ChatClient.builder(chatModel);
    this.chatClient = chatClientBuilder
        .defaultSystem("""
            Extract DESIGNED_BY and PUBLISHED_BY relationships from the given text.
            Only return facts supported by the text.
            """)
        .build();
    this.knowledgeGraph = new KnowledgeGraph(new ArrayList<>());
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
    knowledgeGraph.addAll(extracted.edges());
  }

  //
  // Original finder methods coupled to the board game domin.
  //
//  @Tool(name = "find-game-designers",
//      description = "Find the designers of a board game.")
//  public List<String> findGameDesigners(
//      @ToolParam(description = "The title of the game")
//      String gameTitle) {
//    LOGGER.info("Finding the designers of {}.", gameTitle);
//
//    return knowledgeGraph.edges().stream()
//        .filter(e -> e.from().equals(gameTitle))
//        .filter(e -> e.relation() == Relation.DESIGNED_BY)
//        .map(Edge::to)
//        .toList();
//  }
//
//  @Tool(name = "find-game-publisher",
//      description = "Find the publisher of a board game.")
//  public String findGamePublisher(
//      @ToolParam(description = "The title of the game")
//      String gameTitle) {
//    LOGGER.info("Finding the publisher of {}.", gameTitle);
//
//    return knowledgeGraph.edges().stream()
//        .filter(e -> e.from().equals(gameTitle))
//        .filter(e -> e.relation() == Relation.PUBLISHED_BY)
//        .map(Edge::to)
//        .findFirst()
//        .orElseThrow();
//  }
//
//  @Tool(name = "find-games-by-designer",
//      description = "Find games designed by a designer.")
//  public List<String> findGamesByDesigner(
//      @ToolParam(description = "The designer to find games for")
//      String designer) {
//    LOGGER.info("Finding games designed by {}.", designer);
//
//    return knowledgeGraph.edges().stream()
//        .filter(e -> e.relation() == Relation.DESIGNED_BY)
//        .filter(e -> e.to().equals(designer))
//        .map(Edge::from)
//        .toList();
//  }

  //
  // New finder method that is domain-agnostic
  //
  @Tool(description = """
    Find relationships in a board game knowledge graph.
    Supply at least one of 'from' or 'to'.
    Use 'from' to find relationships originating from
    an entity.
    Use 'to' to find relationships pointing to an entity.
    """)
  public List<Edge> findRelationships(
      @ToolParam(description = "Source entity", required = false)
      String from,
      @ToolParam(description = "Relationship type")
      Relation relation,
      @ToolParam(description = "Target entity", required = false)
      String to) {

    LOGGER.info(
        "Finding relationships in a knowledge graph. {} -- {} --> {}",
        from, relation, to);

    if (from == null && to == null) {
      return List.of();
    }

    return knowledgeGraph.edges().stream()
        .filter(e -> from == null ||
            e.from().equalsIgnoreCase(from))
        .filter(e -> e.relation() == relation)
        .filter(e -> to == null ||
            e.to().equalsIgnoreCase(to))
        .toList();
  }

}
