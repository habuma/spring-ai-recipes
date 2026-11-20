package com.example.agui;

import com.agui.community.core.agent.Agent;
import io.modelcontextprotocol.client.McpSyncClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.mcp.client.initialized=false")
class AgUiApplicationTests {

  // no MCP server is available in tests, so run without any MCP clients
  @TestConfiguration
  static class NoMcpClients {
    @Bean
    @Primary
    List<McpSyncClient> noMcpClients() {
      return List.of();
    }
  }

  @Autowired
  Agent agent;

  @Test
  void contextLoadsWithTheMcpAppsAgent() {
    assertThat(agent).isInstanceOf(McpAppsAgent.class);
  }

}
