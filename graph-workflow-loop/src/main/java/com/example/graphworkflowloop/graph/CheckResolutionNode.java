package com.example.graphworkflowloop.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

public class CheckResolutionNode implements NodeAction {

  private static final Logger logger = LoggerFactory.getLogger(CheckResolutionNode.class);

  private final ChatClient chatClient;

  public CheckResolutionNode(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  @Override
  public Map<String, Object> apply(OverAllState state) {
    String userQuestion = state.value("user_question", "");
    String response = state.value("response", "");
    int attempts = state.value("attempts", 0);

    logger.trace("Attempt {} to resolve {}", attempts+1, userQuestion);

    Evaluation evaluation = chatClient.prompt()
        .system("""
            You are evaluating a customer support response.

            Determine whether the response adequately resolves
            the customer's request.

            A good response should:
            - directly address the user's issue
            - provide clear, actionable steps
            - avoid vague or generic advice

            If the response is insufficient, explain briefly what is missing.

            Respond ONLY as JSON:

            {
              "resolved": true|false,
              "feedback": "..."
            }
            """)
        .user("""
            Customer request:
            %s

            Support response:
            %s
            """.formatted(userQuestion, response))
        .call()
        .entity(Evaluation.class);

    return Map.of(
        "resolved", evaluation.resolved(),
        "feedback", evaluation.feedback(),
        "attempts", attempts + 1
    );
  }

  public record Evaluation(
      boolean resolved,
      String feedback
  ) {}
}
