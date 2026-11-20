package com.example.essentialrag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RerankingDocumentPostProcessorTests {

  @Test
  void documentsAreReorderedAndTrimmedToTopKAccordingToTheModelsRanking() {
    var model = mock(ChatModel.class);
    when(model.getOptions()).thenReturn(ChatOptions.builder().build());
    when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(
        new Generation(new AssistantMessage("{\"documentIndexes\":[2,0,1]}")))));
    var documents = List.of(
        new Document("a", "first", Map.of()),
        new Document("b", "second", Map.of()),
        new Document("c", "third", Map.of()));

    var result = new RerankingDocumentPostProcessor(ChatClient.builder(model), 2)
        .process(new Query("question"), documents);

    assertThat(result).extracting(Document::getId).containsExactly("c", "a");
  }

}
