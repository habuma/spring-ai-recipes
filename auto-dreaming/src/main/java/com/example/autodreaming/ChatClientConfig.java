package com.example.autodreaming;

import org.springaicommunity.agent.advisors.AutoMemoryToolsAdvisor;
import org.springaicommunity.agent.dream.AutoDreamAdvisor;
import org.springaicommunity.agent.dream.AutoDreamService;
import org.springaicommunity.agent.dream.DreamTriggers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
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

  @Bean
  ChatClientBuilderCustomizer addAutoDreamService(
      ChatModel model,
      @Value("${agent.memories.dir}") String rootMemoriesDirectory) {
    return builder -> {
      var dreamService = AutoDreamService.builder(ChatClient.builder(model))
          .build();

      var dreamAdvisor =
          AutoDreamAdvisor.builder()
              .memoriesRootDirectory(rootMemoriesDirectory)
              .dreamService(dreamService)
              .dreamTrigger(DreamTriggers.hoursAndSessions(24, 5))
              .build();

      builder.defaultAdvisors(dreamAdvisor);
    };
  }

}
