package com.example.reactagentfun;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ReactAgentFunApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReactAgentFunApplication.class, args);
  }

  @Bean
  ApplicationRunner go(ChatClient chatClient) {
    return args -> {

      var answer = chatClient.prompt()
          .user("""
              I'm in New Orleans Square.
              Should I ride Pirates of the Caribbean, Haunted Mansion, or Star Tours next?
              """)
          .call()
          .content();

      System.err.println(" --> " + answer);
    };
  }

}
