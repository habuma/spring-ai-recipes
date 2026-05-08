package com.example.graphworkflowloop.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ClassifySupportRequestNode implements NodeAction {

  private static final Logger logger = LoggerFactory.getLogger(ClassifySupportRequestNode.class);

  private final ChatClient chatClient;

  public ClassifySupportRequestNode(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  @Override
  public Map<String, Object> apply(OverAllState state) throws Exception {
    logger.info("Classifying user question");
    var question = state.value("user_question", String.class)
        .orElseThrow();

    var category = chatClient.prompt()
        .system("""
            Classify the customer support request as exactly one of:
            billing
            technical
            unknown

            Respond with only the category name.
            """)
        .user(question)
        .call()
        .content()
        .trim()
        .toLowerCase();

    return Map.of("category", category);
  }

}
