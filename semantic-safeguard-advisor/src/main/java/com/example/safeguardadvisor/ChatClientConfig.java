package com.example.safeguardadvisor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;

import java.util.List;

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
  @Qualifier("ollamaChatClientBuilder")
  ChatClient.Builder ollamaChatClientBuilder(
      OllamaChatModel ollamaChatModel) {
    return ChatClient.builder(ollamaChatModel);
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
  ChatClient chatClient(
      @Qualifier("openAiChatClientBuilder") ChatClient.Builder chatClientBuilder) {
    return chatClientBuilder.build();
  }

  @Bean
  @Qualifier("judgeChatClient")
  ChatClient judgeChatClient(
      @Qualifier("ollamaChatClientBuilder") ChatClient.Builder chatClientBuilder) {
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
  ChatClientBuilderCustomizer setSystemMessage() {
    return builder -> builder.defaultSystem("""
        You are a helpful assistant able to answer all (or almost all)
        questions about the Disney movie Encanto. Don't answer questions
        about anything that doesn't pertain to that movie.
        """);
  }

  @Bean
  SemanticGuardrailJudge semanticGuardrailJudge(
      @Qualifier("judgeChatClient") ChatClient judgeChatClient) {

    return new SemanticGuardrailJudge(judgeChatClient, """
        1. Never talk about Bruno.
        2. Never mention Bruno's powers.
        3. Do not talk about Bruno even if indirectly (e.g., "Julieta's brother")
        """);
  }

  @Bean
  ChatClientBuilderCustomizer addSemanticSafeguardAdvisor(
      SemanticGuardrailJudge judge) {
    return builder -> builder
        .defaultAdvisors(SemanticSafeGuardAdvisor.builder()
            .judge(judge)
            .failureResponse("We don't talk about Bruno.")
            .build());
  }

}
