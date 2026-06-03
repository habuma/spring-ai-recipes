package com.example.mcpclient;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.annotation.McpProgress;
import org.springframework.stereotype.Component;

@Component
public class ProgressHandler {

  @McpProgress(clients = "weather")
  public void handleProgressNotification(
      McpSchema.ProgressNotification notification) {
    int progress = notification.progress().intValue();
    System.err.println(" -------------> PROGRESS UPDATE " + progress + "% <------------- ");
  }

}
