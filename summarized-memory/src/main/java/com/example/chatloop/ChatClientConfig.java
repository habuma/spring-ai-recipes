package com.example.chatloop;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.session.DefaultSessionService;
import org.springframework.ai.session.InMemorySessionRepository;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionMemoryAdvisor;
import org.springframework.ai.session.compaction.RecursiveSummarizationCompactionStrategy;
import org.springframework.ai.session.compaction.TurnCountTrigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

  @Bean
  ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
    return chatClientBuilder.build();
  }

  @Bean
  SessionService sessionService() {
    return DefaultSessionService.builder()
        .sessionRepository(InMemorySessionRepository.builder().build())
        .build();
  }

  @Bean
  ChatClientBuilderCustomizer chatMemoryCustomizer(
      SessionService sessionService, ChatModel chatModel) {
    return builder -> {
      var chatClient = ChatClient.builder(chatModel).build();

      builder.defaultAdvisors(
          SessionMemoryAdvisor.builder(sessionService)
              .defaultUserId("craig")
              .compactionTrigger(new TurnCountTrigger(20))
              .compactionStrategy(
                  RecursiveSummarizationCompactionStrategy.builder(chatClient)
                    .maxEventsToKeep(10)
                    .build())
              .build());
    };
  }

}
