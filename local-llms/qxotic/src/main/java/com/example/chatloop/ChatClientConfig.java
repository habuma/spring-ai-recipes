package com.example.chatloop;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

  @Bean
  ChatClient.Builder chatClientBuilder(
      ChatModel chatModel,
      ObjectProvider<ChatClientBuilderCustomizer> customizers) {
    return applyCustomizers(
        ChatClient.builder(chatModel),
        customizers);
  }

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
  ChatClientBuilderCustomizer addTools(WeatherTools weatherTools) {
    return builder -> builder.defaultTools(weatherTools);
  }

  private ChatClient.Builder applyCustomizers(
      ChatClient.Builder builder,
      ObjectProvider<ChatClientBuilderCustomizer> customizers) {

    customizers.orderedStream()
        .forEach(customizer -> customizer.customize(builder));
    return builder;
  }

}
