package com.example.agui;

import com.agui.community.core.agent.Agent;
import com.agui.community.spring.ai.SpringAiAgent;
import io.modelcontextprotocol.client.McpSyncClient;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.mcp.McpToolNamePrefixGenerator;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AgUiApplication {

  public static void main(String[] args) {
    SpringApplication.run(AgUiApplication.class, args);
  }

  @Bean
  ChatClientBuilderCustomizer pirate() {
    return builder ->
        builder.defaultSystem("You are a pirate");
  }

  @Bean
  Agent mcpAppsAgent(ChatClient.Builder chatClientBuilder, List<McpSyncClient> mcpClients) {
    var mcpTools = SyncMcpToolCallbackProvider.builder()
        .mcpClients(mcpClients)
        .toolNamePrefixGenerator(McpToolNamePrefixGenerator.noPrefix())
        .build();

    Agent springAiAgent = SpringAiAgent.builder(chatClientBuilder.build())
        .tools(List.of(mcpTools.getToolCallbacks()))
        .build();

    return new McpAppsAgent(springAiAgent, mcpClients);
  }

}
