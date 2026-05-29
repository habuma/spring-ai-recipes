package com.example.mcpclient;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.annotation.McpElicitation;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ElicitationHandler {

  @McpElicitation(clients = "product-agent")
  public McpSchema.ElicitResult handleElicitationRequest(McpSchema.ElicitRequest request) {
    if (request.message().equals("isVip")) {
      Map<String, Object> customerInfo = Map.of(
          "customerId", "cust-12345",
          "vip", false
      );
      return new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, customerInfo);
    }

    return new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.DECLINE, null);
  }

}
