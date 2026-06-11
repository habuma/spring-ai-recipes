package com.example.longtermmemory;

import org.springaicommunity.agent.advisors.AutoMemoryToolsAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
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
  ChatClientBuilderCustomizer longTermMemoryCustomizer(
      @Value("${agent.memories.dir}") String rootMemoriesDirectory) {
    return builder -> {
      builder.defaultAdvisors(AutoMemoryToolsAdvisor.builder()
          .memoriesRootDirectory(rootMemoriesDirectory)
          .build());
    };
  }

}
