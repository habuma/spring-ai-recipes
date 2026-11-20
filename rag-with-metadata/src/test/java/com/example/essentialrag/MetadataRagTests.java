package com.example.essentialrag;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class MetadataRagTests {

  static String modelReply = "taco-truck";

  @TestConfiguration
  static class StubModel {
    @Bean
    @Primary
    OpenAiChatModel stubChatModel() {
      var model = Mockito.mock(OpenAiChatModel.class);
      when(model.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
      when(model.call(any(Prompt.class))).thenAnswer(inv -> new ChatResponse(
          List.of(new Generation(new AssistantMessage(modelReply)))));
      return model;
    }
  }

  @MockitoBean(reset = MockReset.NONE)
  VectorStore vectorStore;

  @Autowired
  ChatClient chatClient;

  @Autowired
  TitleHelper titleHelper;

  @Test
  @SuppressWarnings("unchecked")
  void eachIngestedDocumentIsTaggedWithItsTitle() {
    var captor = ArgumentCaptor.forClass(List.class);
    verify(vectorStore, atLeast(3)).accept(captor.capture());

    Set<Object> titles = ((List<List<Document>>) (List<?>) captor.getAllValues())
        .stream().flatMap(List::stream)
        .map(d -> d.getMetadata().get("title"))
        .collect(Collectors.toSet());

    assertThat(titles).containsExactlyInAnyOrder(
        "camp-bowwow", "frog-panic", "taco-truck");
  }

  @Test
  void titleHelperMapsAnswerToTitleOrNullWhenUnknown() {
    modelReply = "taco-truck";
    assertThat(titleHelper.determineGameTitle("How do I play Taco Truck?"))
        .isEqualTo("taco-truck");

    modelReply = "unknown";
    assertThat(titleHelper.determineGameTitle("How do I play chess?")).isNull();
  }

  @Test
  void titleFilterExpressionIsAppliedToTheVectorSearch() {
    modelReply = "answer";
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(List.of(new Document("Some rules")));

    chatClient.prompt("How do I win?")
        .advisors(spec -> {
          spec.param(ChatMemory.CONVERSATION_ID, "test");
          spec.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "title == 'taco-truck'");
        })
        .call().content();

    var captor = ArgumentCaptor.forClass(SearchRequest.class);
    verify(vectorStore, atLeast(1)).similaritySearch(captor.capture());
    assertThat(captor.getValue().getFilterExpression()).isNotNull();
    assertThat(captor.getValue().getFilterExpression().toString()).contains("taco-truck");
  }

}
