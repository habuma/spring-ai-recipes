package com.example.todowritetool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agent.tools.TodoWriteTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

  private static final Logger logger = LoggerFactory.getLogger(ChatClientConfig.class);

  @Autowired
  ApplicationEventPublisher publisher;

  @Bean
  ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
    return chatClientBuilder.build();
  }

  @Bean
  ChatClientCustomizer messageChatAdviser() {
    return builder ->
        builder.defaultAdvisors(
            ToolCallAdvisor.builder()
                .conversationHistoryEnabled(false)
                .build(),
            MessageChatMemoryAdvisor.builder(
                    MessageWindowChatMemory.builder().build())
                .build());
  }

  @Bean
  ChatClientCustomizer todoWriteTool() {
    return builder -> builder
        .defaultTools(
            TodoWriteTool.builder()
                .todoEventHandler(event -> {

                  var todos = event.todos();
                  var completeCount = todos.stream()
                      .filter(todo ->
                          todo.status().equals(TodoWriteTool.Todos.Status.completed))
                      .count();

                  var percentageComplete =
                      Math.round((completeCount * 100.0) / todos.size());

                  logger.info("Event ({}/{} : {}%):",
                      completeCount,
                      event.todos().size(),
                      percentageComplete);
                  event.todos().forEach(todoItem -> {
                    logger.info("   -- TODO Item: {} - {}",
                        todoItem.status(),
                        todoItem.content());
                  });

                })
                .build());
  }

}
