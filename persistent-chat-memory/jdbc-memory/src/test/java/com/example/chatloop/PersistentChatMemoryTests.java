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
 * Runs against a real postgres container (skipped when Docker isn't available).
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class PersistentChatMemoryTests {

  @Container
  static final GenericContainer<?> container = new GenericContainer<>("postgres:latest")
      .withEnv("POSTGRES_DB", "chatmemorydb")
      .withEnv("POSTGRES_USER", "myaiapp")
      .withEnv("POSTGRES_PASSWORD", "secret")
      .withExposedPorts(5432)
      .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 2)
          .withStartupTimeout(Duration.ofMinutes(2)));

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", () -> "jdbc:postgresql://" + container.getHost()
        + ":" + container.getMappedPort(5432) + "/chatmemorydb");
    registry.add("spring.datasource.username", () -> "myaiapp");
    registry.add("spring.datasource.password", () -> "secret");
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
  ChatMemoryRepository chatMemoryRepository;

  @Test
  void messagesRoundTripThroughTheRepository() {
    var conversationId = UUID.randomUUID().toString();

    chatMemoryRepository.saveAll(conversationId, List.of(
        new UserMessage("Hello"), new AssistantMessage("Hi there")));

    assertThat(chatMemoryRepository.findByConversationId(conversationId))
        .extracting(m -> m.getMessageType().name() + ":" + m.getText())
        .containsExactly("USER:Hello", "ASSISTANT:Hi there");
    assertThat(chatMemoryRepository.findConversationIds()).contains(conversationId);

    chatMemoryRepository.deleteByConversationId(conversationId);
    assertThat(chatMemoryRepository.findByConversationId(conversationId)).isEmpty();
  }

  @Test
  void conversationHistoryIsPersistedAndReplayedToTheModel() {
    var conversationId = UUID.randomUUID().toString();

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
    assertThat(chatMemoryRepository.findByConversationId(conversationId)).hasSize(4);
  }

}
