package com.example.chatloop;

import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

public class TechnicalSupportNode implements NodeAction<SupportState> {

  private static final Logger logger = LoggerFactory.getLogger(TechnicalSupportNode.class);

  private final ChatClient chatClient;

  public TechnicalSupportNode(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  @Override
  public Map<String, Object> apply(SupportState state) throws Exception {
    logger.info("Handling technical question.");
    String message = state.getUserQuestion();
    String resolution = chatClient.prompt()
        .system("""
            You are a technical support assistant.
            Write a brief, helpful response to the customer.
            Suggest one or two practical troubleshooting steps or
            ask if they have tried turning it off and then back on again.
            """)
        .user(message)
        .call()
        .content();
    return Map.of("resolution", resolution);
  }

}
