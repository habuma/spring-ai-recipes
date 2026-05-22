package com.example.mcpclient;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.ai.mcp.annotation.McpLogging;
import org.springframework.stereotype.Component;

@Component
public class McpClientLoggingHandler {

  private static final Logger logger =
      LoggerFactory.getLogger(McpClientLoggingHandler.class);

  @McpLogging(clients = "weathermcp")
  public void handleLoggingFromWeatherMcpServer(
      McpSchema.LoggingLevel level, String whoLoggedIt, String message) {

    logger.makeLoggingEventBuilder(Level.valueOf(level.name()))
            .log(message);
  }

}
