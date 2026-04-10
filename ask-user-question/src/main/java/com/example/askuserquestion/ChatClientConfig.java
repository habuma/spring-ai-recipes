package com.example.askuserquestion;

import org.springaicommunity.agent.tools.AskUserQuestionTool;
import org.springaicommunity.agent.utils.CommandLineQuestionHandler;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

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
  ChatClientCustomizer defaultTools(WeatherTools tools) {
    return builder -> builder.defaultTools(tools);
  }

  @Bean
  ChatClientCustomizer chatClientCustomizer() {
    return builder -> {
      builder.defaultAdvisors(
          SimpleLoggerAdvisor.builder()
              .build());
    };
  }

  @Bean
  ChatClientCustomizer askUserQuestionTool() {
    return builder -> builder
        .defaultTools(AskUserQuestionTool.builder()
            .questionHandler(new CommandLineQuestionHandler())
            .build());
  }

  @Bean
  RestClientCustomizer logbookCustomizer(LogbookClientHttpRequestInterceptor interceptor) {
    return restClient -> restClient.requestInterceptor(interceptor);
  }

}
