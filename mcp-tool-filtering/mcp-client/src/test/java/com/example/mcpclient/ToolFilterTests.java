package com.example.mcpclient;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ToolFilterTests {

  private McpSchema.Tool tool(String name) {
    return McpSchema.Tool.builder().name(name).description(name).build();
  }

  @Test
  void allergenToolsAreFilteredOut() {
    var filter = new ToolFilter();

    assertThat(filter.test(null, tool("get-allergens-for-zipcode"))).isFalse();
  }

  @Test
  void otherToolsAreAllowed() {
    var filter = new ToolFilter();

    assertThat(filter.test(null, tool("get-weather-for-zipcode"))).isTrue();
    assertThat(filter.test(null, tool("get-air-quality-for-zipcode"))).isTrue();
  }

}
