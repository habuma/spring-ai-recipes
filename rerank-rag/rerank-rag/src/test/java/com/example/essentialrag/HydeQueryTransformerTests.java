package com.example.essentialrag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.rag.Query;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HydeQueryTransformerTests {

  private ChatModel modelReplying(String text) {
    var model = mock(ChatModel.class);
    when(model.getOptions()).thenReturn(ChatOptions.builder().build());
    when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(
        List.of(new Generation(new AssistantMessage(text)))));
    return model;
  }

  @Test
  void queryTextIsReplacedWithTheHypotheticalDocument() {
    var model = modelReplying("Spring Boot configures beans using auto-configuration.");
    var transformer = HydeQueryTransformer.builder()
        .chatClientBuilder(ChatClient.builder(model))
        .build();

    var result = transformer.transform(new Query("How does auto-configuration work?"));

    assertThat(result.text())
        .isEqualTo("Spring Boot configures beans using auto-configuration.");
  }

  @Test
  void originalQuestionIsSentToTheModel() {
    var model = modelReplying("hypothetical");
    HydeQueryTransformer.builder()
        .chatClientBuilder(ChatClient.builder(model)).build()
        .transform(new Query("How does auto-configuration work?"));

    var captor = org.mockito.ArgumentCaptor.forClass(Prompt.class);
    verify(model).call(captor.capture());
    assertThat(captor.getValue().getContents())
        .contains("How does auto-configuration work?");
  }

  @Test
  void emptyModelResponseIsRejected() {
    var transformer = HydeQueryTransformer.builder()
        .chatClientBuilder(ChatClient.builder(modelReplying("")))
        .build();

    assertThatThrownBy(() -> transformer.transform(new Query("question")))
        .isInstanceOf(IllegalArgumentException.class);
  }

}
