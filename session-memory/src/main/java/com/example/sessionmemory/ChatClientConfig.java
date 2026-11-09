package com.example.sessionmemory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.session.InMemorySessionRepository;
import org.springframework.ai.session.SessionRepository;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionAdvisors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

  @Bean
  ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
    return chatClientBuilder.build();
  }

  @Bean
  SessionRepository sessionRepository() {
    return InMemorySessionRepository.builder().build();
  }

  @Bean
  ChatClientBuilderCustomizer chatMemoryCustomizer(SessionService sessionService) {
    return builder -> builder
        .defaultAdvisors(SessionAdvisors.builder(sessionService).build());
  }

}
