package com.example.chatloop;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Configuration
public class SupportGraphConfiguration {
    static final String CLASSIFY = "classify";
    static final String BILLING = "billing";
    static final String TECHNICAL = "technical";

    @Bean
    CompiledGraph<SupportState> supportGraph(ChatClient chatClient) throws GraphStateException {
      var classifySupportRequestNode = new ClassifySupportRequestNode(chatClient);
      var billingSupportNode = new BillingSupportNode(chatClient);
      var technicalSupportNode = new TechnicalSupportNode(chatClient);

      var stateGraph = new StateGraph<>(Map.of(), initData -> new SupportState(initData))
          .addNode(CLASSIFY, node_async(classifySupportRequestNode))
          .addNode(BILLING, node_async(billingSupportNode))
          .addNode(TECHNICAL, node_async(technicalSupportNode))
          .addEdge(GraphDefinition.START, CLASSIFY)
          .addConditionalEdges(
              CLASSIFY,
              edge_async(state -> {
                String category = state.getCategory();

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
          .addEdge(BILLING, GraphDefinition.END)
          .addEdge(TECHNICAL, GraphDefinition.END);

      return stateGraph.compile();
    }

}
