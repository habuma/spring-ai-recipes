///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25
//BOM org.springframework.boot:spring-boot-dependencies:4.1.1
//DEPS org.springframework.ai:spring-ai-starter-model-openai:2.0.1
//DEPS com.fasterxml.jackson.core:jackson-annotations:2.21
//SOURCES ChatClientConfig.java

package com.example.chatloop;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class ChatLoopApplication {

    public static void main(String... args) {
        SpringApplication.run(ChatLoopApplication.class, args);
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