package com.example.streaminghttpmcpserver;

import io.modelcontextprotocol.client.McpClient;
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
  void weatherToolIsExposedOverStreamableHttp() {
    var transport = HttpClientStreamableHttpTransport
        .builder("http://localhost:" + port).endpoint("/mcp").build();
    try (var client = McpClient.sync(transport)
        .requestTimeout(Duration.ofSeconds(10)).build()) {
      client.initialize();

      assertThat(client.listTools().tools())
          .extracting(McpSchema.Tool::name)
          .contains("get-weather-for-zipcode");

      var result = client.callTool(new McpSchema.CallToolRequest(
          "get-weather-for-zipcode", Map.of("zipcode", "81657")));

      assertThat(result.isError()).isNotEqualTo(true);
      assertThat(result.content().toString()).contains("81657");
    }
  }

}
