package com.example.graphworkflow;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.graphworkflow.graph.BillingSupportNode;
import com.example.graphworkflow.graph.ClassifySupportRequestNode;
import com.example.graphworkflow.graph.TechnicalSupportNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

@Configuration
public class SupportGraphConfiguration {

  static final String CLASSIFY = "classify";
  static final String BILLING = "billing";
  static final String TECHNICAL = "technical";

  @Bean
  CompiledGraph supportGraph(ChatClient chatClient)
      throws GraphStateException {

    return new StateGraph("support-triage", this::stateStrategies)
        //Nodes
        .addNode(CLASSIFY,
            node_async(new ClassifySupportRequestNode(chatClient)))
        .addNode(BILLING,
            node_async(new BillingSupportNode(chatClient)))
        .addNode(TECHNICAL,
            node_async(new TechnicalSupportNode(chatClient)))

        // Edges
        .addEdge(START, CLASSIFY)
        .addConditionalEdges(
            CLASSIFY,
            edge_async(state -> {
              String category = state.value("category", String.class)
                  .orElse(TECHNICAL);

              return switch (category) {
                case BILLING -> BILLING;
                case TECHNICAL -> TECHNICAL;
                default -> TECHNICAL;
              };
            }),
            Map.of(
                BILLING, BILLING,
                TECHNICAL, TECHNICAL
            ))
        .addEdge(BILLING, END)
        .addEdge(TECHNICAL, END)
        .compile();
  }

  private Map<String, KeyStrategy> stateStrategies() {
    return Map.of(
        "user_question", new ReplaceStrategy(),
        "category", new ReplaceStrategy(),
        "response", new ReplaceStrategy()
    );
  }

}
