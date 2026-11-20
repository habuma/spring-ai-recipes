package com.example.essentialrag;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockReset;
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
@SpringBootTest(properties = "rag.document.url=classpath:/test-document.txt")
class RagWiringTests {

  @MockitoBean
  OpenAiChatModel chatModel;

  // reset = NONE so the startup ingestion call is still visible to the tests
  @MockitoBean(reset = MockReset.NONE)
  VectorStore vectorStore;

  @Autowired
  ChatClient chatClient;

  @Test
  void chatClientIsWiredWithConfiguredAdvisorsAndTools() {
    when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(
        List.of(new Document("The secret rule is forty-two.")));
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
    verify(chatModel, atLeast(2)).call(captor.capture());
    var calls = captor.getAllValues();
    var first = calls.get(calls.size() - 2);
    var second = calls.get(calls.size() - 1);

    // the second call carries the first exchange from chat memory
    assertThat(second.getInstructions())
        .extracting(m -> m.getMessageType())
        .contains(MessageType.USER, MessageType.ASSISTANT);
    assertThat(second.getContents()).contains("Hello");
    // retrieved documents are folded into the prompt by the RAG advisor
    assertThat(first.getContents()).contains("The secret rule is forty-two.");
  }

  @Test
  @SuppressWarnings("unchecked")
  void documentIsIngestedIntoTheVectorStoreAtStartup() {
    var captor = ArgumentCaptor.forClass(List.class);
    verify(vectorStore, atLeast(1)).accept(captor.capture());

    var documents = (List<Document>) captor.getValue();
    assertThat(documents).isNotEmpty();
    assertThat(documents.get(0).getText()).contains("Sagrada");
  }

}
