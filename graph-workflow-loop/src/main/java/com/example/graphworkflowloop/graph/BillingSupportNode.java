package com.example.graphworkflowloop.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BillingSupportNode implements NodeAction {

  private static final Logger logger = LoggerFactory.getLogger(BillingSupportNode.class);
  private final ChatClient chatClient;

  public BillingSupportNode(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  @Override
  public Map<String, Object> apply(OverAllState state) throws Exception {
    logger.info("Handling billing question.");
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

    var response = chatClient.prompt()
        .system("""
            You are a billing support assistant.
            Write a brief, helpful response to the customer.
            Do not promise refunds. Say that the billing team will review the account.
            """)
        .user(userMessage)
        .call()
        .content();

    return Map.of("response", response);
  }

}
