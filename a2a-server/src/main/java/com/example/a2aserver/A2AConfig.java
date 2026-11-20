package com.example.a2aserver;

import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.spec.AgentCapabilities;
import org.a2aproject.sdk.spec.AgentCard;
import org.a2aproject.sdk.spec.AgentInterface;
import org.a2aproject.sdk.spec.AgentSkill;
import org.a2aproject.sdk.spec.TransportProtocol;
import org.springaicommunity.a2a.server.executor.DefaultAgentExecutor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class A2AConfig {

  @Bean
  public AgentExecutor agentExecutor(ChatClient chatClient) {
    return new DefaultAgentExecutor(chatClient, (chat, ctx) -> {
      String userMessage = DefaultAgentExecutor
          .extractTextFromMessage(ctx.getMessage());
      return chat.prompt().user(userMessage).call().content();
    });
  }

  @Bean
  public AgentCard agentCard(
      @Value("${server.host:localhost}") String host,
      @Value("${server.port:8080}") int port) {
    return AgentCard.builder()
        .name("My Weather Agent")
        .description("Provides weather information for a location")
        .version("1.2.3")
        .capabilities(AgentCapabilities.builder().streaming(false).build())
        .defaultInputModes(List.of("text"))
        .defaultOutputModes(List.of("text"))
        .skills(List.of(AgentSkill.builder()
            .id("my_agent").name("My Weather Agent")
            .description("Provides weather information for a location")
            .tags(List.of("weather")).build()))
        .supportedInterfaces(List.of(new AgentInterface(
            TransportProtocol.JSONRPC.asString(),
            "http://" + host + ":" + port + "/a2a/")))
        .build();
  }

}
