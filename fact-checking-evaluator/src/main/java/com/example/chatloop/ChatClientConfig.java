package com.example.chatloop;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class ChatClientConfig {

  @Bean
  @Primary
  ChatClient.Builder openAiChatClientBuilder(
      OpenAiChatModel openAiChatModel,
      ObjectProvider<ChatClientBuilderCustomizer> customizers) {
    return applyCustomizers(
        ChatClient.builder(openAiChatModel),
        customizers
    );
  }

  @Bean
  @Qualifier("minicheck")
  ChatClient.Builder minicheckChatClientBuilder(OllamaChatModel ollamaChatModel) {
    var options = ChatOptions.builder()
        .model("bespoke-minicheck:7b")
        .temperature(0.0);
    return ChatClient.builder(ollamaChatModel).defaultOptions(options);
  }

  private ChatClient.Builder applyCustomizers(
      ChatClient.Builder builder,
      ObjectProvider<ChatClientBuilderCustomizer> customizers) {

    customizers.orderedStream()
        .forEach(customizer -> customizer.customize(builder));
    return builder;
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

}
