package com.example.safeguardadvisor;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class SemanticSafeguardTests {

  static final AtomicReference<String> judgeVerdict = new AtomicReference<>();

  private static ChatResponse reply(String text) {
    return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
  }

  @TestConfiguration
  static class StubModels {
    @Bean
    OpenAiChatModel openAiChatModel() {
      var model = Mockito.mock(OpenAiChatModel.class);
      when(model.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
      when(model.call(any(Prompt.class))).thenReturn(reply("Mirabel has a family."));
      return model;
    }

    @Bean
    OllamaChatModel ollamaChatModel() {
      var model = Mockito.mock(OllamaChatModel.class);
      when(model.getOptions()).thenReturn(OllamaChatOptions.builder().build());
      when(model.call(any(Prompt.class))).thenAnswer(inv -> reply(judgeVerdict.get()));
      return model;
    }
  }

  @Autowired
  ChatClient chatClient;

  private String ask(String question) {
    return chatClient.prompt(question)
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, question))
        .call().content();
  }

  @Test
  void judgeVerdictOfNotAllowedProducesTheFailureResponse() {
    judgeVerdict.set("""
        {"allowed": false, "reason": "mentions Bruno", "violatedRule": "1"}""");

    assertThat(ask("What about Julieta's brother?"))
        .isEqualTo("We don't talk about Bruno.");
  }

  @Test
  void judgeVerdictOfAllowedLetsTheModelAnswerThrough() {
    judgeVerdict.set("""
        {"allowed": true, "reason": "fine", "violatedRule": ""}""");

    assertThat(ask("Who is Mirabel?")).isEqualTo("Mirabel has a family.");
  }

}
