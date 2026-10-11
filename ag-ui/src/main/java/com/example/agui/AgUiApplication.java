package com.example.agui;

import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AgUiApplication {

  public static void main(String[] args) {
    SpringApplication.run(AgUiApplication.class, args);
  }

  @Bean
  ChatClientBuilderCustomizer pirate() {
    return builder ->
        builder.defaultSystem("You are a pirate");
  }

}
