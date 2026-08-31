package com.example.toolsearchtool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.toolsearch.ToolSearchToolCallingAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.tool.toolsearch.ToolIndex;
import org.springframework.ai.tool.toolsearch.index.lucene.LuceneToolIndex;
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
  LuceneToolIndex luceneToolIndex() {
    return new LuceneToolIndex(0.4f);
  }

  @Bean
  ChatClientBuilderCustomizer addToolSearchToolCallingAdvisor(ToolIndex toolIndex) {
    return builder -> {
      var toolSearchAdvisor = ToolSearchToolCallingAdvisor.builder()
          .toolIndex(toolIndex)
          .maxResults(5)
          .build();

      builder.defaultAdvisors(toolSearchAdvisor);
    };
  }

  @Bean
  ChatClientBuilderCustomizer addTools(Toolbox tools, ToolIndex toolIndex) {
    return builder -> {
      builder.defaultTools(tools);
    };
  }

  @Bean
  ChatClientBuilderCustomizer addLogger() {
    return builder ->
        builder.defaultAdvisors(SimpleLoggerAdvisor.builder().build());
  }

}
