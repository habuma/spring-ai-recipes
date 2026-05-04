package com.example.graphworkflow;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@SpringBootApplication
public class GraphWorkflowApplication {

  public static void main(String[] args) {
    SpringApplication.run(GraphWorkflowApplication.class, args);
  }

  @Bean
  ApplicationRunner go(CompiledGraph compiledGraph) {
    return args -> {
      System.out.println("How can I help?\n");
      try (Scanner scanner = new Scanner(System.in)) {
        while (true) {
          System.out.print("> ");
          Map<String, Object> initialState = new HashMap<>();
          initialState.put("user_question", scanner.nextLine());
          var state = compiledGraph.invoke(initialState).orElseThrow();
          System.out.println("\n - " + state.data().get("response"));
        }
      }
    };
  }

}
