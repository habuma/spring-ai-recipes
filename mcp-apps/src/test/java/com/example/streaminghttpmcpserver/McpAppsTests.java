package com.example.streaminghttpmcpserver;

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
class McpAppsTests {

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
  void weatherToolPointsAtTheAppUi() {
    try (var client = client()) {
      var tool = client.listTools().tools().stream()
          .filter(t -> t.name().equals("get-weather-for-zipcode"))
          .findFirst().orElseThrow();
      assertThat(tool.meta()).containsKey("ui");

      var result = client.callTool(new McpSchema.CallToolRequest(
          "get-weather-for-zipcode", Map.of("zipcode", "81657")));
      assertThat(result.content().toString()).contains("81657");
    }
  }

  @Test
  void weatherHtmlResourceIsServed() {
    try (var client = client()) {
      assertThat(client.listResources().resources())
          .extracting(McpSchema.Resource::uri)
          .contains("ui://weather/weather.html");

      var contents = client.readResource(
          new McpSchema.ReadResourceRequest("ui://weather/weather.html"));
      assertThat(contents.contents()).isNotEmpty();
      assertThat(contents.contents().get(0))
          .isInstanceOfSatisfying(McpSchema.TextResourceContents.class,
              text -> assertThat(text.text()).containsIgnoringCase("html"));
    }
  }

}
