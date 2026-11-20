package com.example.chatloop;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Offline tests: the model is mocked, so no API key or Ollama is needed. */
class FactCheckingTests {

  private ChatModel modelReplying(String text) {
    var model = mock(ChatModel.class);
    when(model.getOptions()).thenReturn(ChatOptions.builder().build());
    when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(
        List.of(new Generation(new AssistantMessage(text)))));
    return model;
  }

  private boolean passes(ChatModel model, String claim) {
    var evaluator = FactCheckingEvaluator.builder(ChatClient.builder(model))
        .build();
    var request = new EvaluationRequest("Where is the conference?",
        List.of(Document.builder().text("The conference is in Austin.").build()),
        claim);
    return evaluator.evaluate(request).isPass();
  }

  @Test
  void yesFromTheModelMeansTheClaimIsSupported() {
    assertThat(passes(modelReplying("YES"), "The conference is in Austin.")).isTrue();
  }

  @Test
  void noFromTheModelMeansTheClaimIsNotSupported() {
    assertThat(passes(modelReplying("NO"), "The conference is in Dallas.")).isFalse();
  }

  @Test
  void claimAndSupportingDocumentAreSentToTheModel() {
    var model = modelReplying("YES");

    passes(model, "The conference is in Austin.");

    var captor = ArgumentCaptor.forClass(Prompt.class);
    verify(model).call(captor.capture());
    assertThat(captor.getValue().getContents())
        .contains("The conference is in Austin.");
  }

}
