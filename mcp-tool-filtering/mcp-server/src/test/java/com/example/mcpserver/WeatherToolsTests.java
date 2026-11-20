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
class WeatherToolsTests {

  @LocalServerPort
  int port;

  @Test
  void allThreeToolsAreExposedAndCallable() {
    var transport = HttpClientStreamableHttpTransport
        .builder("http://localhost:" + port).endpoint("/mcp").build();
    try (var client = McpClient.sync(transport)
        .requestTimeout(Duration.ofSeconds(10)).build()) {
      client.initialize();

      assertThat(client.listTools().tools())
          .extracting(McpSchema.Tool::name)
          .containsExactlyInAnyOrder("get-weather-for-zipcode",
              "get-air-quality-for-zipcode", "get-allergens-for-zipcode");

      var air = client.callTool(new McpSchema.CallToolRequest(
          "get-air-quality-for-zipcode", Map.of("zipcode", "81657")));
      assertThat(air.isError()).isNotEqualTo(true);
      assertThat(air.content().toString()).contains("87");

      var allergens = client.callTool(new McpSchema.CallToolRequest(
          "get-allergens-for-zipcode", Map.of("zipcode", "81657")));
      assertThat(allergens.isError()).isNotEqualTo(true);
    }
  }

}
