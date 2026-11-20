package com.example.semanticcaching;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Runs against a real Qdrant container (skipped when Docker isn't available).
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = "spring.docker.compose.enabled=false")
class SemanticCachingTests {

  @Container
  static final GenericContainer<?> container = new GenericContainer<>("qdrant/qdrant:latest")
      .withExposedPorts(6334)
      .waitingFor(Wait.forLogMessage(".*gRPC listening on.*", 1)
          .withStartupTimeout(Duration.ofMinutes(2)));

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.ai.vectorstore.qdrant.host", container::getHost);
    registry.add("spring.ai.vectorstore.qdrant.port", () -> container.getMappedPort(6334));
  }

  @TestConfiguration
  static class Stubs {
    @Bean
    @Primary
    OpenAiChatModel stubChatModel() {
      var model = Mockito.mock(OpenAiChatModel.class);
      when(model.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
      when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(
          List.of(new Generation(new AssistantMessage("Paris")))));
      return model;
    }

    // deterministic embeddings: identical text gives an identical vector
    @Bean
    @Primary
    EmbeddingModel fakeEmbeddingModel() {
      return new EmbeddingModel() {
        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
          var embeddings = new ArrayList<Embedding>();
          int i = 0;
          for (String text : request.getInstructions()) {
            embeddings.add(new Embedding(vector(text), i++));
          }
          return new EmbeddingResponse(embeddings);
        }

        @Override
        public float[] embed(org.springframework.ai.document.Document document) {
          return vector(document.getText());
        }

        @Override
        public int dimensions() {
          return 8;
        }

        float[] vector(String text) {
          var vector = new float[8];
          for (int i = 0; i < text.length(); i++) {
            vector[i % 8] += text.charAt(i);
          }
          float norm = 0;
          for (float v : vector) norm += v * v;
          norm = (float) Math.sqrt(norm);
          for (int i = 0; i < 8; i++) vector[i] /= norm;
          return vector;
        }
      };
    }
  }

  @Autowired
  OpenAiChatModel chatModel;

  @Autowired
  ChatClient chatClient;

  private String ask(String question, String conversationId) {
    return chatClient.prompt(question)
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
        .call().content();
  }

  @Test
  void repeatedQuestionsAreServedFromTheSemanticCache() {
    var question = "What is the capital of France? (" + System.nanoTime() + ")";

    assertThat(ask(question, "a")).isEqualTo("Paris");
    assertThat(ask(question, "b")).isEqualTo("Paris");

    verify(chatModel, times(1)).call(any(Prompt.class));
  }

}
