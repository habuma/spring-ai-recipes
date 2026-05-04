package com.example.graphworkflow.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

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

            Respond with only the category name.
            """)
        .user(question)
        .call()
        .content()
        .trim()
        .toLowerCase();

    if (!category.equals("billing") && !category.equals("technical")) {
      logger.info("Question category({}) unknown. Defaulting to technical.", category);
      category = "technical";
    }

    return Map.of("category", category);
  }

}
