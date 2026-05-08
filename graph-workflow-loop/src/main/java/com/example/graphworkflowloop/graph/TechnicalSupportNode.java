package com.example.graphworkflowloop.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TechnicalSupportNode implements NodeAction {

  private static final Logger logger = LoggerFactory.getLogger(TechnicalSupportNode.class);

  private final ChatClient chatClient;

  public TechnicalSupportNode(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  @Override
  public Map<String, Object> apply(OverAllState state) throws Exception {
    logger.info("Handling technical question.");
    String question = state.value("user_question", String.class)
        .orElseThrow();
    String feedback = state.value("feedback", "");

    String userMessage = """
    Customer request:
    %s
    """.formatted(question);

    if (!feedback.isBlank()) {
      userMessage += """
      
      Previous feedback on your last response:
      %s
      
      Revise your answer to address this feedback.
      """.formatted(feedback);
    }

    String response = chatClient.prompt()
        .system("""
            You are a technical support assistant.
            Write a brief, helpful response to the customer.
            Suggest one or two practical troubleshooting steps or
            ask if they have tried turning it off and then back on again.
            """)
        .user(userMessage)
        .call()
        .content();

    return Map.of("response", response);
  }
}
