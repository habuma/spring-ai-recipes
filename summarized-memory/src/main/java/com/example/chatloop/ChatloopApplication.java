package com.example.chatloop;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.session.advisor.SessionMemoryAdvisor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class ChatloopApplication {

  public static void main(String[] args) {
    SpringApplication.run(ChatloopApplication.class, args);
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
          var response = chatClient.prompt(input)
              .advisors(a -> a.param(SessionMemoryAdvisor.SESSION_ID_CONTEXT_KEY, "DEMO"))
              .call()
              .chatResponse();
          var tokens = response.getMetadata().getUsage();
          var answer = response.getResult().getOutput().getText();
          System.out.printf("\n - %s%n", answer);
          System.out.printf("          (Token usage: %d input, %d output, %d total)%n\n\n",
              tokens.getPromptTokens(), tokens.getCompletionTokens(), tokens.getTotalTokens());
        }
      }
    };
  }

}
