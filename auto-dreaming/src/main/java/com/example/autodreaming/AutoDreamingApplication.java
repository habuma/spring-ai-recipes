package com.example.autodreaming;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Scanner;

@SpringBootApplication
@EnableAsync
public class AutoDreamingApplication {

  public static void main(String[] args) {
    SpringApplication.run(AutoDreamingApplication.class, args);
  }

  @Bean
  ApplicationRunner go(
      ChatClient chatClient,
      DreamRunner dreamRunner,
      @Value("${agent.memories.dir}") String rootMemoriesDirectory) {
    return args -> {
      System.out.println("How can I help?\n");

      try (Scanner scanner = new Scanner(System.in)) {
        while (true) {
          System.out.print("> ");
          if (!scanner.hasNextLine()) break;
          var input = scanner.nextLine();
          if (input.isBlank()) continue;

          if (input.equals("/dream")) {
            dreamRunner.dream(rootMemoriesDirectory);
            System.out.println("Dreaming in the background...");
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
