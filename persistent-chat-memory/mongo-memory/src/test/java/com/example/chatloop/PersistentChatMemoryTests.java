package com.example.chatloop;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Runs against a real mongo container (skipped when Docker isn't available).
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class PersistentChatMemoryTests {

  @Container
  static final GenericContainer<?> container = new GenericContainer<>("mongo:latest")
      .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
      .withEnv("MONGO_INITDB_ROOT_PASSWORD", "secret")
      .withExposedPorts(27017)
      .waitingFor(Wait.forLogMessage(".*Waiting for connections.*", 1)
          .withStartupTimeout(Duration.ofMinutes(2)));

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.mongodb.uri", () -> "mongodb://root:secret@" + container.getHost()
        + ":" + container.getMappedPort(27017) + "/chatmemorydb?authSource=admin");
  }

  @TestConfiguration
  static class StubModel {
    @Bean
    @Primary
    OpenAiChatModel stubChatModel() {
      var model = Mockito.mock(OpenAiChatModel.class);
      when(model.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
      when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(
          List.of(new Generation(new AssistantMessage("Mocked reply")))));
      return model;
    }
  }

  @Autowired
  OpenAiChatModel chatModel;

  @Autowired
  ChatClient chatClient;

  @Autowired
  ChatMemoryRepository repository;

  @Test
  void messagesRoundTripThroughTheRepository() {
    var conversationId = "roundtrip-" + UUID.randomUUID();

    repository.saveAll(conversationId, List.of(
        new UserMessage("Hello"), new AssistantMessage("Hi there")));

    assertThat(repository.findByConversationId(conversationId))
        .extracting(m -> m.getMessageType().name() + ":" + m.getText())
        .containsExactly("USER:Hello", "ASSISTANT:Hi there");
    assertThat(repository.findConversationIds()).contains(conversationId);

    repository.deleteByConversationId(conversationId);
    assertThat(repository.findByConversationId(conversationId)).isEmpty();
  }

  @Test
  void conversationHistoryIsPersistedAndReplayedToTheModel() {
    var conversationId = "chat-" + UUID.randomUUID();

    chatClient.prompt("Remember the word pineapple")
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
        .call().content();
    chatClient.prompt("What was the word?")
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
        .call().content();

    var captor = ArgumentCaptor.forClass(Prompt.class);
    verify(chatModel, atLeast(2)).call(captor.capture());
    var lastPrompt = captor.getValue();

    assertThat(lastPrompt.getContents()).contains("pineapple");
    assertThat(repository.findByConversationId(conversationId)).hasSize(4);
  }

}
