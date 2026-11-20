package com.example.chatloop;

import org.junit.jupiter.api.Test;
import org.springframework.ai.support.ToolCallbacks;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherToolsTests {

  @Test
  void toolIsDiscoveredFromAnnotationsAndCallableWithJsonArguments() {
    var callbacks = ToolCallbacks.from(new WeatherTools());

    assertThat(callbacks).hasSize(1);
    assertThat(callbacks[0].getToolDefinition().name())
        .isEqualTo("get-temperature-for-zipcode");

    var result = callbacks[0].call("{\"zipcode\":\"81657\"}");

    assertThat(result).contains("81657").contains("78");
  }

}
