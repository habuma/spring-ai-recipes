package com.example.essentialrag;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
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
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "rag.document.url=classpath:/test-document.txt")
class ConversationAwareRagTests {

  // stubbed before the context starts, because the app reads the model's
  // default options while building its ChatClients
  @TestConfiguration
  static class StubModel {
    @Bean
    @Primary
    OpenAiChatModel stubChatModel() {
      var model = Mockito.mock(OpenAiChatModel.class);
      when(model.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
      when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(
          List.of(new Generation(new AssistantMessage("Standalone question")))));
      return model;
    }
  }

  @MockitoBean(reset = MockReset.NONE)
  VectorStore vectorStore;

  @Autowired
  ChatClient chatClient;

  @Test
  void retrievalUsesTheQueryRewrittenByTheCompressionTransformer() {
    when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(
        List.of(new Document("The secret rule is forty-two.")));

    chatClient.prompt("What is the secret rule?")
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "test")).call().content();
    chatClient.prompt("And how is it applied?")
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "test")).call().content();

    var captor = ArgumentCaptor.forClass(SearchRequest.class);
    verify(vectorStore, atLeast(2)).similaritySearch(captor.capture());
    var queries = captor.getAllValues().stream().map(SearchRequest::getQuery).toList();

    // retrieval uses the query rewritten by the compression transformer
    assertThat(queries.get(queries.size() - 1)).isEqualTo("Standalone question");
  }

}
