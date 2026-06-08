package com.example.chatclientcustomizer;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
  @ConditionalOnProperty("chatclient.tools.weather.enabled")
  ChatClientBuilderCustomizer defaultTools(WeatherTools tools) {
    return builder -> builder.defaultTools(tools);
  }

  @Bean
  @ConditionalOnProperty("chatclient.pirate.enabled")
  ChatClientBuilderCustomizer pirateTalk() {
    return builder -> builder.defaultSystem("Talk like a pirate");
  }

}
