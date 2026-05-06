package com.example.graphworkflowhitl;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

@SpringBootApplication
public class GraphWorkflowHitlApplication {

  public static void main(String[] args) {
    SpringApplication.run(GraphWorkflowHitlApplication.class, args);
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

          // NEW: Create a runnable config with a unique thread ID.
          //      This is what enables the flow to be resumed later.
          RunnableConfig runnableConfig = RunnableConfig.builder()
              .threadId(UUID.randomUUID().toString())
              .build();

          var state = compiledGraph.invoke(initialState, runnableConfig).orElseThrow();

          // NEW: If the category is "unknown", ask the user for classification.
          //      Then resume the flow to get the final answer.
          if (state.data().get("category").equals("unknown")) {
            RunnableConfig resumeConfig = askUserForClassification(compiledGraph, scanner, runnableConfig);
            state = compiledGraph.invoke(Map.of(), resumeConfig).orElseThrow();
          }

          System.out.println("\n - " + state.data().get("response"));
        }
      }
    };
  }

  private static RunnableConfig askUserForClassification(
      CompiledGraph supportGraph,
      Scanner scanner,
      RunnableConfig runnableConfig) throws Exception {

    String humanCategory = "";
    while (!humanCategory.equals("billing")
        && !humanCategory.equals("technical")) {

      System.out.print("How would you categorize your question? [billing or technical]: ");
      humanCategory = scanner.nextLine()
          .trim()
          .toLowerCase();
    }

    RunnableConfig resumeConfig = supportGraph
        .getState(runnableConfig)
        .config()
        .withResume();

    resumeConfig = supportGraph.updateState(
        resumeConfig,
        Map.of("category", humanCategory),
        "human-classify"
    );
    return resumeConfig;
  }

}
