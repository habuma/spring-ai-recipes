package com.example.askuserquestion;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies the ChatClient wiring (advisors, tools, system prompt) end to end
 * against a mocked model, so no API key or network access is needed.
 */
@SpringBootTest
class ChatClientWiringTests {

  @MockitoBean
  OpenAiChatModel chatModel;

  @Autowired
  ChatClient chatClient;

  @Test
  void chatClientIsWiredWithConfiguredAdvisorsAndTools() {
    when(chatModel.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
    when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
        List.of(new Generation(new AssistantMessage("Mocked reply")))));

    assertThat(chatClient.prompt("Hello")
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "test"))
        .call().content()).isEqualTo("Mocked reply");
    assertThat(chatClient.prompt("Again")
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "test"))
        .call().content()).isEqualTo("Mocked reply");

    var captor = ArgumentCaptor.forClass(Prompt.class);
    verify(chatModel, times(2)).call(captor.capture());
    var first = captor.getAllValues().get(0);
    var second = captor.getAllValues().get(1);

    assertThat(((ToolCallingChatOptions) first.getOptions()).getToolCallbacks())
        .extracting(cb -> cb.getToolDefinition().name())
        .contains("get-weather-for-zipcode", "AskUserQuestionTool");

    // the second call carries the first exchange from chat memory
    assertThat(second.getInstructions())
        .extracting(m -> m.getMessageType())
        .contains(MessageType.USER, MessageType.ASSISTANT);
    assertThat(second.getContents()).contains("Hello");
  }

}
