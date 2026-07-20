package com.example.chatloop;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.PostgresChatMemoryRepositoryDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class ChatClientConfig {

  @Bean
  ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
    return chatClientBuilder.build();
  }

  @Bean
  ChatClientBuilderCustomizer chatMemoryCustomizer(ChatMemory chatMemory) {
    return builder -> {
      builder.defaultAdvisors(
          MessageChatMemoryAdvisor.builder(chatMemory)
              .build());
    };
  }

  @Bean
  ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
    return MessageWindowChatMemory.builder()
        .chatMemoryRepository(chatMemoryRepository)
        .maxMessages(500)
        .build();
  }

  @Bean
  ChatMemoryRepository chatMemoryRepository(DataSource dataSource) {
    return JdbcChatMemoryRepository.builder()
        .dialect(new PostgresChatMemoryRepositoryDialect())
        .dataSource(dataSource)
        .build();
  }



}
