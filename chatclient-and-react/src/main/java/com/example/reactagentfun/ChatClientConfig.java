package com.example.reactagentfun;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

  @Bean
  ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
    return chatClientBuilder.build();
  }

  @Bean
  ChatClientBuilderCustomizer addTools(DisneyParksTools disneyParksTools) {
    return builder -> {
      builder.defaultTools(disneyParksTools);
    };
  }

  @Bean
  ChatClientBuilderCustomizer addLoggingAdvisor() {
    return builder -> {
      builder.defaultAdvisors(SimpleLoggerAdvisor.builder().build());
    };
  }

}
