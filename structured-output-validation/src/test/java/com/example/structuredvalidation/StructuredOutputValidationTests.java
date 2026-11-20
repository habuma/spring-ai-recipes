package com.example.structuredvalidation;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StructuredOutputValidationTests {

  private static ChatResponse reply(String text) {
    return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
  }

  @Test
  void invalidJsonIsRetriedUntilTheModelProducesValidOutput() {
    var model = mock(ChatModel.class);
    when(model.getOptions()).thenReturn(ChatOptions.builder().build());
    when(model.call(any(Prompt.class))).thenReturn(
        reply("Sure! Here are the top songs: not json at all"),
        reply("{\"year\":\"1985\",\"songs\":"
            + "[{\"title\":\"Careless Whisper\",\"artist\":\"Wham!\"}]}"));

    var advisor = StructuredOutputValidationAdvisor.builder()
        .outputType(TopSongs.class)
        .maxRepeatAttempts(5)
        .build();

    var topSongs = ChatClient.builder(model).build().prompt()
        .user("Top songs of 1985?")
        .advisors(advisor)
        .call()
        .entity(TopSongs.class);

    assertThat(topSongs.year()).isEqualTo("1985");
    assertThat(topSongs.songs()).containsExactly(
        new Song("Careless Whisper", "Wham!"));
    verify(model, atLeast(2)).call(any(Prompt.class));
  }

}
