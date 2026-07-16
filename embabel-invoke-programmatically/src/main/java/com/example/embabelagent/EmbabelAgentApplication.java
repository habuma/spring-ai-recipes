package com.example.embabelagent;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class EmbabelAgentApplication {

  public static void main(String[] args) {
    SpringApplication.run(EmbabelAgentApplication.class, args);
  }

  @Bean
  ApplicationRunner go(AgentPlatform agentPlatform) {
    return args -> {
      System.out.println("How can I help?\n");

      var invocation = AgentInvocation.builder(agentPlatform)
          .build(SupportResponse.class);

      try (Scanner scanner = new Scanner(System.in)) {
        while (true) {
          System.out.print("> ");
          if (!scanner.hasNextLine()) break; // to avoid infinite loops in tests
          var input = scanner.nextLine();
          if (input.isBlank()) continue; // allows user to hit return without error
          var response = invocation.invoke(new UserInput(input));
          System.out.println("\n - " + response.text());
        }
      }
    };
  }

}
