package com.example.mcpserver;

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
class CruiseToolsTests {

  @LocalServerPort
  int port;

  McpSyncClient client(boolean sampling) {
    var transport = HttpClientStreamableHttpTransport
        .builder("http://localhost:" + port).endpoint("/mcp").build();
    var builder = McpClient.sync(transport)
        .requestTimeout(Duration.ofSeconds(10));
    if (sampling) {
      builder.capabilities(McpSchema.ClientCapabilities.builder()
              .sampling().build())
          .sampling(request -> McpSchema.CreateMessageResult.builder()
              .role(McpSchema.Role.ASSISTANT)
              .content(new McpSchema.TextContent("A lovely Alaskan cruise."))
              .model("test-model")
              .build());
    }
    var client = builder.build();
    client.initialize();
    return client;
  }

  private McpSchema.CallToolResult call(McpSyncClient client) {
    return client.callTool(new McpSchema.CallToolRequest(
        "get-cruise-writeup", Map.of("cruiseId", "1")));
  }

  @Test
  void writeupIsGeneratedByClientSampling() {
    try (var client = client(true)) {
      var result = call(client);
      assertThat(result.isError()).isNotEqualTo(true);
      assertThat(result.content().toString()).contains("A lovely Alaskan cruise.");
    }
  }

  @Test
  void fallsBackWhenClientDoesNotSupportSampling() {
    try (var client = client(false)) {
      assertThat(call(client).content().toString())
          .contains("Unable to produce cruise writeup.");
    }
  }

}
