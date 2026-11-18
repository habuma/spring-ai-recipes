package com.example.reasoningsummary;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class ReasoningSummaryApplication {

  private static final String CYAN = "\u001B[36m";
  private static final String RESET = "\u001B[0m";

  public static void main(String[] args) {
    SpringApplication.run(ReasoningSummaryApplication.class, args);
  }

  @Bean
  ApplicationRunner go(ChatClient chatClient) {
    return args -> {
      System.out.println("How can I help?\n");

      try (Scanner scanner = new Scanner(System.in)) {
        while (true) {
          System.out.print("> ");
          if (!scanner.hasNextLine()) break;
          var input = scanner.nextLine();
          if (input.isBlank()) continue;

          var chatResponse = chatClient.prompt(input)
              .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, "DEMO"))
              .call()
              .chatResponse();

          emitReasoningSummaries(chatResponse);

          var answer = chatResponse.getResult().getOutput().getText();
          System.out.println("\n - " + answer);
        }
      }
    };
  }

  private void emitReasoningSummaries(ChatResponse chatResponse) {
    var reasoningParts = chatResponse.getResult().getOutput().getReasoning();
    reasoningParts.forEach( part -> {
      if (part.summary() != null) {
      System.out.println(CYAN + part.summary() + RESET);
      }
    });
  }

}
