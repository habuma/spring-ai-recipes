package com.example.structuredvalidation;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class StructuredValidationApplicationTests {

  // The application runner calls the model at startup, so the model is stubbed
  // to keep the context test independent of a running Ollama.
  @TestConfiguration
  static class StubModel {
    @Bean
    @Primary
    OllamaChatModel stubChatModel() {
      var model = Mockito.mock(OllamaChatModel.class);
      when(model.getOptions()).thenReturn(OllamaChatOptions.builder().build());
      when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(
          new Generation(new AssistantMessage(
              "{\"year\":\"1985\",\"songs\":[{\"title\":\"Careless Whisper\",\"artist\":\"Wham!\"}]}")))));
      return model;
    }
  }

  @Test
  void contextLoads() {
  }

}
