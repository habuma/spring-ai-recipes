package com.example.safeguardadvisor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.List;

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
  ChatClientBuilderCustomizer setSystemMessage() {
    return builder -> builder.defaultSystem("""
        You are a helpful assistant able to answer all (or almost all)
        questions about the Disney movie Encanto. Don't answer questions
        about anything that doesn't pertain to that movie.
        """);
  }

  @Bean
  ChatClientBuilderCustomizer addSafeguardAdvisor() {
    var safeguard = SafeGuardAdvisor.builder()
        .sensitiveWords(List.of("Bruno", "bruno", "BRUNO", "vision", "prophecy"))
        .failureResponse("We don't talk about Bruno.")
        .order(Ordered.HIGHEST_PRECEDENCE)
        .build();

    return builder ->
      builder.defaultAdvisors(safeguard);
  }

  @Bean
  ChatClientBuilderCustomizer addOutputSafeGuardAdvisor() {
    var saferGuard = OutputSafeGuardAdvisor.builder()
        .sensitiveWords("Bruno", "bruno", "BRUNO", "vision", "prophecy")
        .failureResponse("We don't talk about Bruno.")
        .build();

    return builder ->
      builder.defaultAdvisors(saferGuard);
  }

}
