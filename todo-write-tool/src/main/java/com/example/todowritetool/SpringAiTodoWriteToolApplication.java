package com.example.todowritetool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class SpringAiTodoWriteToolApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringAiTodoWriteToolApplication.class, args);
  }

  @Bean
  ApplicationRunner go(ChatClient chatClient) {
    return args -> {
      System.out.println("How can I help?\n");
      try (Scanner scanner = new Scanner(System.in)) {
        while (true) {
          System.out.print("> ");
          System.out.println("\n - " +
              chatClient.prompt(scanner.nextLine()).call().content());
        }
      }
    };
  }

}
