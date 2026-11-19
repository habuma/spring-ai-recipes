package com.example.simplegraphrag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

  @Bean
  ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
    return chatClientBuilder.build();
  }

  @Bean
  ChatClientBuilderCustomizer chatMemoryCustomizer() {
    return builder -> {
      builder.defaultAdvisors(
          MessageChatMemoryAdvisor.builder(
                  MessageWindowChatMemory.builder()
                      .maxMessages(500)
                      .build())
              .build());
    };
  }

  @Bean
  ChatClientBuilderCustomizer addGraphSearchTools(KnowledgeGraphService knowledgeGraphService) {
    return builder -> builder.defaultTools(knowledgeGraphService);
  }

  @Bean
  ChatClientBuilderCustomizer strictToolUseOnly() {
    return builder -> builder.defaultSystem("""
        You are a helpful assistant answering questions about board games.
        When asked questions about board games, only answer questions
        using information from the available tools. 
        """);
  }

}
