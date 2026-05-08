package com.example.graphworkflowloop.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;

import java.util.Map;

public class EscalateToHumanNode implements NodeAction {

  @Override
  public Map<String, Object> apply(OverAllState state) {

    String userQuestion = state.value("user_question", "");
    String category = state.value("category", "unknown");
    String response = state.value("response", "");
    String feedback = state.value("feedback", "");
    int attempts = state.value("attempts", 0);

    String finalResponse = """
        I wasn't able to fully resolve this automatically, so this should be escalated
        to a human support representative.

        Category: %s

        Original request:
        %s

        Last attempted response:
        %s

        Evaluation feedback:
        %s

        Automated attempts: %d
        """.formatted(category, userQuestion, response, feedback, attempts);

    return Map.of(
        "escalated", true,
        "finalResponse", finalResponse
    );
  }
}