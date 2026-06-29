package com.example.multillm;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.chat.client.autoconfigure.ChatClientBuilderConfigurer;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatClientConfig {

  @Bean
  @Qualifier("openAiChatClientBuilder")
  ChatClient.Builder openAiChatClientBuilder(
      OpenAiChatModel openAiChatModel,
      ObjectProvider<ChatClientBuilderCustomizer> customizers) {

    return applyCustomizers(
        ChatClient.builder(openAiChatModel),
        customizers
    );
  }

  @Bean
  @Qualifier("anthropicChatClientBuilder")
  ChatClient.Builder anthropicChatClientBuilder(
      AnthropicChatModel anthropicChatModel,
      ObjectProvider<ChatClientBuilderCustomizer> customizers) {

    return applyCustomizers(
        ChatClient.builder(anthropicChatModel),
        customizers
    );
  }

  private ChatClient.Builder applyCustomizers(
      ChatClient.Builder builder,
      ObjectProvider<ChatClientBuilderCustomizer> customizers) {

    customizers.orderedStream()
        .forEach(customizer -> customizer.customize(builder));

    return builder;
  }

  @Bean
  @Primary
  @Qualifier("openAiChatClient")
  ChatClient openAiChatClient(@Qualifier("openAiChatClientBuilder") ChatClient.Builder chatClientBuilder) {
    return chatClientBuilder.build();
  }

  @Bean
  @Qualifier("anthropicChatClient")
  ChatClient anthropicChatClient(@Qualifier("anthropicChatClientBuilder") ChatClient.Builder chatClientBuilder) {
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

}
