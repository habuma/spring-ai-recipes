package com.example.mcpclient;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.annotation.McpSampling;
import org.springframework.stereotype.Component;

@Component
public class SamplingProvider {

  private final ChatModel chatModel;

  public SamplingProvider(ChatModel chatModel) {
    this.chatModel = chatModel;
  }

  @McpSampling(clients = "cruising")
  public McpSchema.CreateMessageResult handleSamplingRequest(McpSchema.CreateMessageRequest request) {
    var systemPrompt = request.systemPrompt();
    var response = chatModel.call(new Prompt(systemPrompt));
    return McpSchema.CreateMessageResult.builder()
        .role(McpSchema.Role.ASSISTANT)
        .content(new McpSchema.TextContent(
            response.getResult().getOutput().getText()))
        .model(response.getMetadata().getModel())
        .build();
  }

}
