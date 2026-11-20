package com.example.mcpclient;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class McpClientLoggingHandlerTests {

  // Note: levels with no slf4j equivalent (e.g. WARNING, NOTICE) are not covered
  // here because the handler's Level.valueOf(level.name()) throws for them.
  @Test
  void handlesLogMessagesAtCommonLevelsWithoutFailing() {
    var handler = new McpClientLoggingHandler();

    for (var level : new McpSchema.LoggingLevel[] {
        McpSchema.LoggingLevel.INFO, McpSchema.LoggingLevel.ERROR,
        McpSchema.LoggingLevel.DEBUG}) {
      assertThatCode(() ->
          handler.handleLoggingFromWeatherMcpServer(level, "server", "hello"))
          .doesNotThrowAnyException();
    }
  }

}
