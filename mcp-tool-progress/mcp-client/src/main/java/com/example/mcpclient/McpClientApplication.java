package com.example.mcpclient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

@SpringBootApplication
public class McpClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(McpClientApplication.class, args);
  }

  @Bean
  ApplicationRunner go(ChatClient chatClient) {
    return args -> {
      System.out.println("How can I help?\n");
      try (Scanner scanner = new Scanner(System.in)) {
        while (true) {
          System.out.print("> ");
          System.out.println("\n - " +
              chatClient.prompt(scanner.nextLine())
                  .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, "DEMO"))
                  .toolContext(Map.of("progressToken", UUID.randomUUID().toString()))
                  .call().content());
        }
      }
    };
  }

}
