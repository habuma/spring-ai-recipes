package com.example.multillm;

import org.junit.jupiter.api.Test;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class MultiLlmTests {

  private static ChatResponse reply(String text) {
    return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
  }

  // stubbed before the context starts because the app's ChatClients read the
  // models' default options while they are being built
  @TestConfiguration
  static class StubModels {
    @Bean
    OpenAiChatModel openAiChatModel() {
      var model = Mockito.mock(OpenAiChatModel.class);
      when(model.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
      when(model.call(any(Prompt.class))).thenReturn(reply("from OpenAI"));
      return model;
    }

    @Bean
    AnthropicChatModel anthropicChatModel() {
      var model = Mockito.mock(AnthropicChatModel.class);
      when(model.getOptions()).thenReturn(AnthropicChatOptions.builder().build());
      when(model.call(any(Prompt.class))).thenReturn(reply("from Anthropic"));
      return model;
    }
  }

  @Autowired
  @Qualifier("openAiChatClient")
  ChatClient openAiChatClient;

  @Autowired
  @Qualifier("anthropicChatClient")
  ChatClient anthropicChatClient;

  private String ask(ChatClient client, String question) {
    return client.prompt(question)
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "test"))
        .call().content();
  }

  @Test
  void eachChatClientTalksToItsOwnModel() {
    assertThat(ask(openAiChatClient, "hi")).isEqualTo("from OpenAI");
    assertThat(ask(anthropicChatClient, "hi")).isEqualTo("from Anthropic");
  }

}
