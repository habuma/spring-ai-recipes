package com.example.securedmcpserver;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
class SecuredServerTests {

  @LocalServerPort
  int port;

  @Test
  void requestsWithoutApiKeyAreRejected() throws Exception {
    var response = HttpClient.newHttpClient().send(
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/mcp"))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json, text/event-stream")
            .POST(HttpRequest.BodyPublishers.ofString("{}")).build(),
        HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isIn(401, 403);
  }

  @Test
  void requestsWithApiKeyCanCallTools() {
    var transport = HttpClientStreamableHttpTransport
        .builder("http://localhost:" + port).endpoint("/mcp")
        .requestBuilder(HttpRequest.newBuilder()
            .header("X-MCP-API-KEY", "agent1.somesecret"))
        .build();
    try (var client = McpClient.sync(transport)
        .requestTimeout(Duration.ofSeconds(10)).build()) {
      client.initialize();

      var result = client.callTool(new McpSchema.CallToolRequest(
          "get-weather-for-zipcode", Map.of("zipcode", "81657")));
      assertThat(result.isError()).isNotEqualTo(true);
      assertThat(result.content().toString()).contains("81657");
    }
  }

}
