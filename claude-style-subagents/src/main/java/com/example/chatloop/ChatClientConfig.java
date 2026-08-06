package com.example.chatloop;

import org.springaicommunity.agent.tools.task.TaskTool;
import org.springaicommunity.agent.tools.task.claude.ClaudeSubagentReferences;
import org.springaicommunity.agent.tools.task.claude.ClaudeSubagentType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
public class ChatClientConfig {

  @Bean
  ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
    return chatClientBuilder.build();
  }

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
  @Qualifier("subagentChatClientBuilder")
  ChatClient.Builder subagentChatClientBuilder(
      OpenAiChatModel openAiChatModel) {
    return ChatClient.builder(openAiChatModel);
  }

  private ChatClient.Builder applyCustomizers(
      ChatClient.Builder builder,
      ObjectProvider<ChatClientBuilderCustomizer> customizers) {

    customizers.orderedStream()
        .forEach(customizer -> customizer.customize(builder));

    return builder;
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

  @Value("${agent.tasks.paths}")
  List<Resource> agentPaths;

  @Bean
  ChatClientBuilderCustomizer addClaudeAgents(
      @Value("${agent.tasks.paths}") List<Resource> agentPaths,
      @Qualifier("subagentChatClientBuilder")  ChatClient.Builder subagentChatClientBuilder) {
    return builder -> {
      ToolCallback toolCallback = TaskTool.builder()
          .subagentReferences(
              ClaudeSubagentReferences.fromResources(agentPaths))
          .subagentTypes(ClaudeSubagentType.builder()
              .chatClientBuilder("default", subagentChatClientBuilder)
              .build())
          .build();

      builder.defaultTools(toolCallback)
          .defaultSystem("When telling jokes, always use the joke-teller agent.");
    };
  }

}
