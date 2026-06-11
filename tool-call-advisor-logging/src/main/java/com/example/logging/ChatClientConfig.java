package com.example.logging;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
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
  ChatClientBuilderCustomizer defaultTools(WeatherTools tools) {
    return builder -> builder.defaultTools(tools);
  }

  @Bean
  ChatClientBuilderCustomizer addSimpleLoggerAdvisor() {
    return builder -> {
      builder.defaultAdvisors(
          SimpleLoggerAdvisor.builder()
              .build());
    };
  }

// No longer needs to be explicitly configured. It is the default as of
// Spring AI 2.0.0-M7
//  @Bean
//  ChatClientBuilderCustomizer addToolCallAdvisor() {
//    return builder ->
//        builder.defaultAdvisors(
//            ToolCallAdvisor.builder()
//                .advisorOrder(BaseAdvisor.HIGHEST_PRECEDENCE + 300)
//                .disableInternalConversationHistory()
//                .build());
//  }

}
