package com.example.safeguardadvisor;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
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
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class SafeguardTests {

  static String modelReply = "Mirabel has a family.";

  @TestConfiguration
  static class StubModel {
    @Bean
    @Primary
    OpenAiChatModel stubChatModel() {
      var model = Mockito.mock(OpenAiChatModel.class);
      when(model.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
      when(model.call(any(Prompt.class))).thenAnswer(inv ->
          new ChatResponse(List.of(new Generation(new AssistantMessage(modelReply)))));
      return model;
    }
  }

  @Autowired
  OpenAiChatModel chatModel;

  @Autowired
  ChatClient chatClient;

  private String ask(String question) {
    return chatClient.prompt(question)
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, question))
        .call().content();
  }

  @Test
  void questionsContainingSensitiveWordsAreBlockedBeforeReachingTheModel() {
    Mockito.clearInvocations(chatModel);

    assertThat(ask("Tell me about bruno")).isEqualTo("We don't talk about Bruno.");

    verify(chatModel, never()).call(any(Prompt.class));
  }

  @Test
  void modelResponsesContainingSensitiveWordsAreReplaced() {
    modelReply = "Well, Bruno saw a vision of the future.";

    assertThat(ask("Who is Mirabel's uncle?")).isEqualTo("We don't talk about Bruno.");
  }

  @Test
  void cleanResponsesPassThrough() {
    modelReply = "Mirabel has a family.";

    assertThat(ask("Who is Mirabel?")).isEqualTo("Mirabel has a family.");
  }

}
