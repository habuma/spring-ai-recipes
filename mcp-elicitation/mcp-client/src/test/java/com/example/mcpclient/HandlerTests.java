package com.example.mcpclient;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HandlerTests {

  @Test
  void vipElicitationIsAcceptedWithCustomerInfo() {
    var request = McpSchema.ElicitRequest.builder().message("isVip")
        .requestedSchema(Map.of()).build();

    var result = new ElicitationHandler().handleElicitationRequest(request);

    assertThat(result.action()).isEqualTo(McpSchema.ElicitResult.Action.ACCEPT);
    assertThat(result.content()).containsEntry("vip", false);
  }

  @Test
  void otherElicitationsAreDeclined() {
    var request = McpSchema.ElicitRequest.builder().message("something else")
        .requestedSchema(Map.of()).build();

    var result = new ElicitationHandler().handleElicitationRequest(request);

    assertThat(result.action()).isEqualTo(McpSchema.ElicitResult.Action.DECLINE);
  }

  @Test
  void samplingRequestsAreAnsweredByTheChatModel() {
    var chatModel = mock(ChatModel.class);
    when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
        List.of(new Generation(new AssistantMessage("Sampled text")))));
    var request = McpSchema.CreateMessageRequest.builder()
        .systemPrompt("Describe a cruise")
        .messages(List.of())
        .maxTokens(100)
        .build();

    var result = new SamplingProvider(chatModel).handleSamplingRequest(request);

    assertThat(result.role()).isEqualTo(McpSchema.Role.ASSISTANT);
    assertThat(((McpSchema.TextContent) result.content()).text())
        .isEqualTo("Sampled text");
  }

}
