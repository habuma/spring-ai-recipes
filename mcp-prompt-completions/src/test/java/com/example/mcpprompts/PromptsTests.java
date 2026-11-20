package com.example.mcpprompts;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "server.port=0")
class PromptsTests {

  @LocalServerPort
  int port;

  McpSyncClient client() {
    var transport = HttpClientStreamableHttpTransport
        .builder("http://localhost:" + port).endpoint("/mcp").build();
    var client = McpClient.sync(transport)
        .requestTimeout(Duration.ofSeconds(10)).build();
    client.initialize();
    return client;
  }

  @Test
  void promptsAreListedAndRendered() {
    try (var client = client()) {
      assertThat(client.listPrompts().prompts())
          .extracting(McpSchema.Prompt::name)
          .contains("summarize-text", "get-weather-for-city");

      var prompt = client.getPrompt(new McpSchema.GetPromptRequest(
          "get-weather-for-city", Map.of("city-name", "Denver")));
      assertThat(prompt.messages().toString())
          .contains("What is the weather in Denver");
    }
  }

  @Test
  void cityNamesAreCompleted() {
    try (var client = client()) {
      var result = client.completeCompletion(new McpSchema.CompleteRequest(
          new McpSchema.PromptReference("get-weather-for-city"),
          new McpSchema.CompleteRequest.CompleteArgument("city-name", "San")));

      assertThat(result.completion().values())
          .contains("San Antonio", "San Diego", "San Jose", "San Francisco")
          .doesNotContain("Denver");
    }
  }

}
