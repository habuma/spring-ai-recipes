package com.example.sessionmemory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.session.advisor.SessionAdvisors;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class SessionMemoryApplication {

  public static void main(String[] args) {
    SpringApplication.run(SessionMemoryApplication.class, args);
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
                  .advisors(SessionAdvisors.session("DEMO"))
                  .call()
                  .content());
        }
      }
    };
  }

}
