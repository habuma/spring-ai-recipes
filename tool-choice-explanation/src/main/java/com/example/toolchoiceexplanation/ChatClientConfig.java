package com.example.toolchoiceexplanation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.tool.augment.AugmentedToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

  private static final Logger logger = LoggerFactory.getLogger(ChatClientConfig.class);

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
  ChatClientBuilderCustomizer addToolExplanation(Toolbox tools) {
    return builder -> {
      var provider =
          AugmentedToolCallbackProvider.<ToolChoiceExplanation>builder()
              .toolObject(tools)
              .argumentType(ToolChoiceExplanation.class)
              .argumentConsumer(event -> {
                ToolChoiceExplanation thinking = event.arguments();
                logger.debug("Tool called : {}", event.toolDefinition().name());
                logger.debug("Reasoning   : {}", thinking.innerThought());
                logger.debug("Confidence  : {}", thinking.confidence());
                logger.debug("MemoryNotes : {}", thinking.memoryNotes());
              })
              .build();
      builder.defaultTools(provider);
    };
  }

}
