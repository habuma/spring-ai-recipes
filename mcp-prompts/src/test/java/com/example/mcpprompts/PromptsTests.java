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

  @Test
  void summarizePromptIsListedAndRendered() {
    var transport = HttpClientStreamableHttpTransport
        .builder("http://localhost:" + port).endpoint("/mcp").build();
    try (var client = McpClient.sync(transport)
        .requestTimeout(Duration.ofSeconds(10)).build()) {
      client.initialize();

      assertThat(client.listPrompts().prompts())
          .extracting(McpSchema.Prompt::name)
          .contains("summarize-text");

      var prompt = client.getPrompt(new McpSchema.GetPromptRequest(
          "summarize-text", Map.of("text", "Some text to condense.")));
      assertThat(prompt.messages().toString())
          .contains("Summarize the given text", "Some text to condense.");
    }
  }

}
