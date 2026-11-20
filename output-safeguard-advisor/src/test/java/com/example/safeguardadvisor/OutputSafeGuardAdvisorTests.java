package com.example.safeguardadvisor;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OutputSafeGuardAdvisorTests {

  private final OutputSafeGuardAdvisor advisor = OutputSafeGuardAdvisor.builder()
      .sensitiveWords("Bruno")
      .failureResponse("blocked")
      .build();

  private String adviseWithModelReply(String reply) {
    var request = new ChatClientRequest(new Prompt("hi"), Map.of());
    var chain = mock(CallAdvisorChain.class);
    when(chain.nextCall(any())).thenReturn(ChatClientResponse.builder()
        .chatResponse(new ChatResponse(
            List.of(new Generation(new AssistantMessage(reply)))))
        .build());

    return advisor.adviseCall(request, chain)
        .chatResponse().getResult().getOutput().getText();
  }

  @Test
  void replacesResponsesContainingSensitiveWords() {
    assertThat(adviseWithModelReply("Bruno did it")).isEqualTo("blocked");
  }

  @Test
  void leavesOtherResponsesAlone() {
    assertThat(adviseWithModelReply("Mirabel did it")).isEqualTo("Mirabel did it");
  }

}
