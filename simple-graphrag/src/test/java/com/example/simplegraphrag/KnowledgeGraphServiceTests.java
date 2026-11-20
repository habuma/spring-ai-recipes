package com.example.simplegraphrag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KnowledgeGraphServiceTests {

  private KnowledgeGraphService serviceWhoseModelExtracts(String json) {
    var model = mock(ChatModel.class);
    when(model.getOptions()).thenReturn(ChatOptions.builder().build());
    when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(
        List.of(new Generation(new AssistantMessage(json)))));
    return new KnowledgeGraphService(model);
  }

  private KnowledgeGraphService populatedService() {
    var service = serviceWhoseModelExtracts("""
        {"edges":[
          {"from":"Sagrada","relation":"DESIGNED_BY","to":"Daryl Andrews"},
          {"from":"Sagrada","relation":"PUBLISHED_BY","to":"Floodgate Games"},
          {"from":"Azul","relation":"DESIGNED_BY","to":"Michael Kiesling"}]}""");
    service.addToKnowledgeGraph("some text about board games");
    return service;
  }

  @Test
  void relationshipsExtractedByTheModelAreQueryableBySource() {
    assertThat(populatedService().findRelationships("sagrada", Relation.DESIGNED_BY, null))
        .containsExactly(new Edge("Sagrada", Relation.DESIGNED_BY, "Daryl Andrews"));
  }

  @Test
  void relationshipsAreQueryableByTarget() {
    assertThat(populatedService().findRelationships(null, Relation.PUBLISHED_BY, "Floodgate Games"))
        .containsExactly(new Edge("Sagrada", Relation.PUBLISHED_BY, "Floodgate Games"));
  }

  @Test
  void queryWithoutSourceOrTargetReturnsNothing() {
    assertThat(populatedService().findRelationships(null, Relation.DESIGNED_BY, null)).isEmpty();
  }

}
