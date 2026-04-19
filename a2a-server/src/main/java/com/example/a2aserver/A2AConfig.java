package com.example.a2aserver;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
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
    return new AgentCard.Builder()
        .name("My Weather Agent")
        .description("Provides weather information for a location")
        .url("http://" + host + ":" + port + "/a2a/")
        .version("1.2.3")
        .capabilities(new AgentCapabilities.Builder().streaming(false).build())
        .defaultInputModes(List.of("text"))
        .defaultOutputModes(List.of("text"))
        .skills(List.of(new AgentSkill.Builder()
            .id("my_agent").name("My Weather Agent")
            .description("Provides weather information for a location")
            .tags(List.of("weather")).build()))
        .protocolVersion("0.3.0")
        .build();
  }

}
