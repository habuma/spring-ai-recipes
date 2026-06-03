package com.example.mcpclient;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.McpConnectionInfo;
import org.springframework.ai.mcp.McpToolFilter;
import org.springframework.stereotype.Component;

@Component
public class ToolFilter implements McpToolFilter {

  private static final Logger logger = LoggerFactory.getLogger(ToolFilter.class);

  @Override
  public boolean test(McpConnectionInfo mcpConnectionInfo,
                      McpSchema.Tool tool) {

    if (tool.name().contains("allergen")) {
      logger.info("Filtering out tool: " + tool.name());
      return false;
    }

    logger.info("Allowing tool: " + tool.name());
    return true;
  }

}
