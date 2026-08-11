package com.example.essentialrag;

import com.integrallis.vectors.core.filter.Filters;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class EssentialRagApplication {

  public static void main(String[] args) {
    SpringApplication.run(EssentialRagApplication.class, args);
  }

  @Bean
  ApplicationRunner go(ChatClient chatClient, TitleHelper titleHelper) {
    return args -> {
      System.out.println("How can I help?\n");

      try (Scanner scanner = new Scanner(System.in)) {
        while (true) {
          System.out.print("> ");
          if (!scanner.hasNextLine()) break; // to avoid infinite loops in tests
          var input = scanner.nextLine();
          if (input.isBlank()) continue; // allows user to hit return without error

          var requestSpec = chatClient.prompt(input);

          var gameTitle = titleHelper.determineGameTitle(input);

          var answer = requestSpec
              .advisors(spec -> {
                spec.param(ChatMemory.CONVERSATION_ID, "DEMO");
                if (gameTitle != null) {
                  spec.param(
                      QuestionAnswerAdvisor.FILTER_EXPRESSION,
                      String.format("title == '%s'", gameTitle));
                }
              })
              .call()
              .content();

          System.out.println("\n - " + answer);
        }
      }
    };
  }

}
