package com.example.multillm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class MultiLLMApplication {

  public static void main(String[] args) {
    SpringApplication.run(MultiLLMApplication.class, args);
  }

  @Bean
  ApplicationRunner go(
      @Qualifier("openAiChatClient") ChatClient openAiChatClient,
      @Qualifier("anthropicChatClient") ChatClient anthropicChatClient) {
    return args -> {
      ChatClient chatClient = openAiChatClient;

      System.out.println("How can I help?\n");

      try (Scanner scanner = new Scanner(System.in)) {
        while (true) {
          System.out.print("> ");
          if (!scanner.hasNextLine()) break; // to avoid infinite loops in tests
          var input = scanner.nextLine();
          if (input.isBlank()) continue; // allows user to hit return without error

          if (input.toLowerCase().equals("/openai")) {
            chatClient = openAiChatClient;
            System.out.println("::: Using OpenAI");
            continue;
          } else if (input.toLowerCase().equals("/anthropic")) {
            chatClient = anthropicChatClient;
            System.out.println("::: Using Anthropic");
            continue;
          }

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
