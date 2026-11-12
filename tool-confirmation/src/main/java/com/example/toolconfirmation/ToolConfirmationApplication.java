package com.example.toolconfirmation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.session.advisor.SessionAdvisors;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;
import java.util.UUID;

@SpringBootApplication
public class ToolConfirmationApplication {

  public static void main(String[] args) {
    SpringApplication.run(ToolConfirmationApplication.class, args);
  }

  @Bean
  ApplicationRunner go(ChatClient chatClient, ToolConfirmationHandler confirmationHandler) {
    return args -> {
      String sessionId = UUID.randomUUID().toString();

      System.out.println("How can I help?\n");

      try (Scanner scanner = new Scanner(System.in)) {
        while (true) {
          System.out.print("> ");
          if (!scanner.hasNextLine()) break;
          var input = scanner.nextLine();
          if (input.isBlank()) continue;

          var response = chatClient.prompt(input)
              .advisors(SessionAdvisors.session(sessionId))
              .call()
              .chatResponse();
          response = confirmationHandler.handle(response, scanner, sessionId);
          var answer = response.getResult().getOutput().getText();
          System.out.println("\n - " + answer);
        }
      }
    };
  }

}
