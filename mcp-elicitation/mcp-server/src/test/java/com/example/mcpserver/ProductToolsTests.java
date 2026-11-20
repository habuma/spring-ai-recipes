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
class ProductToolsTests {

  @LocalServerPort
  int port;

  McpSyncClient client(boolean vip) {
    var transport = HttpClientStreamableHttpTransport
        .builder("http://localhost:" + port).endpoint("/mcp").build();
    var builder = McpClient.sync(transport)
        .requestTimeout(Duration.ofSeconds(10));
    if (vip) {
      builder.capabilities(McpSchema.ClientCapabilities.builder()
              .elicitation().build())
          .elicitation(request -> new McpSchema.ElicitResult(
              McpSchema.ElicitResult.Action.ACCEPT, Map.of("vip", true)));
    }
    var client = builder.build();
    client.initialize();
    return client;
  }

  private McpSchema.CallToolResult call(McpSyncClient client, String sku) {
    return client.callTool(new McpSchema.CallToolRequest(
        "get-product-details", Map.of("sku", sku)));
  }

  @Test
  void nonExclusiveProductsAreReturnedWithoutElicitation() {
    try (var client = client(false)) {
      var result = call(client, "sku-1");
      assertThat(result.isError()).isNotEqualTo(true);
      assertThat(result.content().toString()).contains("Solo Cup");
    }
  }

  @Test
  void exclusiveProductIsRefusedWhenCustomerIsNotVip() {
    try (var client = client(false)) {
      assertThat(call(client, "sku-99999").isError()).isTrue();
    }
  }

  @Test
  void exclusiveProductIsReturnedWhenVipCustomerAcceptsElicitation() {
    try (var client = client(true)) {
      var result = call(client, "sku-99999");
      assertThat(result.isError()).isNotEqualTo(true);
      assertThat(result.content().toString()).contains("gold");
    }
  }

}
