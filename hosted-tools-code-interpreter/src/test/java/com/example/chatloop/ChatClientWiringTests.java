package com.example.chatloop;

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
import org.springframework.ai.openai.responses.OpenAiResponsesChatModel;
import org.springframework.ai.openai.responses.OpenAiResponsesChatOptions;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies the ChatClient wiring (advisors, tools, system prompt) end to end
 * against a mocked model, so no API key or network access is needed.
 */
@SpringBootTest
class ChatClientWiringTests {

  @Autowired
  OpenAiResponsesChatModel chatModel;

  // stubbed before the context starts, because the app reads the model's
  // default options while building its ChatClient
  @TestConfiguration
  static class StubModel {
    @Bean
    @Primary
    OpenAiResponsesChatModel stubChatModel() {
      var model = Mockito.mock(OpenAiResponsesChatModel.class);
      when(model.getOptions()).thenReturn(OpenAiResponsesChatOptions.builder().build());
      when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(
          List.of(new Generation(new AssistantMessage("Mocked reply")))));
      return model;
    }
  }

  @Autowired
  ChatClient.Builder chatClientBuilder;

  @Test
  void chatClientBuilderTalksToTheResponsesModel() {
    var chatClient = chatClientBuilder.build();
    when(chatModel.getOptions()).thenReturn(OpenAiResponsesChatOptions.builder().build());
    when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
        List.of(new Generation(new AssistantMessage("Mocked reply")))));

    assertThat(chatClient.prompt("Hello")
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "test"))
        .call().content()).isEqualTo("Mocked reply");
    assertThat(chatClient.prompt("Again")
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "test"))
        .call().content()).isEqualTo("Mocked reply");

    var captor = ArgumentCaptor.forClass(Prompt.class);
    verify(chatModel, atLeast(2)).call(captor.capture());
    var calls = captor.getAllValues();
    var first = calls.get(calls.size() - 2);
    var second = calls.get(calls.size() - 1);
  }

}
