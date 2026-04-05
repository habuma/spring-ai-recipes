package com.example.chatclientcustomizer;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
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
  ChatClientCustomizer chatMemoryCustomizer() {
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
  ChatClientCustomizer defaultTools(WeatherTools tools) {
    return buidler -> buidler.defaultTools(tools);
  }

  @Bean
  @ConditionalOnProperty("chatclient.pirate.enabled")
  ChatClientCustomizer pirateTalk() {
    return builder -> builder.defaultSystem("Talk like a pirate");
  }

}
