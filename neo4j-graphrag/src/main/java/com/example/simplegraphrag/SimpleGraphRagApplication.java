package com.example.simplegraphrag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import java.util.Scanner;

@SpringBootApplication
public class SimpleGraphRagApplication {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleGraphRagApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(SimpleGraphRagApplication.class, args);
  }

  @Bean
  @Order(-1000)
  ApplicationRunner loadData(KnowledgeGraphService knowledgeGraphService) {
    return args -> {
      LOGGER.info("Loading knowledge graph...");
      knowledgeGraphService.addToKnowledgeGraph("""
          Ticket to Ride is a railway-themed board game
          designed by Alan R. Moon and published by Days
          of Wonder.
          
          Fantastic Factories is an engine-building game
          about constructing factories and producing goods.
          It was designed by Joseph Z. Chen and Justin
          Faulkner and published by Metafactory Games.
          
          Honeypot is a card game about secret agent bears
          collecting intel and avoiding traps. It was
          designed by Joseph Z. Chen and published by
          Flatout Games.
          """);
    };
  }

  @Bean
  ApplicationRunner go(ChatClient chatClient) {
    return args -> {
      System.out.println("How can I help?\n");

      try (Scanner scanner = new Scanner(System.in)) {
        while (true) {
          System.out.print("> ");
          if (!scanner.hasNextLine()) break; // to avoid infinite loops in tests
          var input = scanner.nextLine();
          if (input.isBlank()) continue; // allows user to hit return without error
          System.out.println("\n - " +
              chatClient.prompt(input)
                  .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, "DEMO"))
                  .call()
                  .content());
        }
      }
    };
  }

}
