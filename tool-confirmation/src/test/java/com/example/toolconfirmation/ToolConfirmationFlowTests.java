package com.example.toolconfirmation;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.session.advisor.SessionAdvisors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class ToolConfirmationFlowTests {

  private static final String SEND_EMAIL_ARGS = """
      {"email":{"recipient":"alice@example.com","subject":"Hi","body":"Hello"}}""";

  @TestConfiguration
  static class StubModel {
    @Bean
    @Primary
    OpenAiChatModel stubChatModel() {
      var model = Mockito.mock(OpenAiChatModel.class);
      when(model.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
      return model;
    }
  }

  @Autowired
  OpenAiChatModel chatModel;

  @Autowired
  ChatClient chatClient;

  @Autowired
  ToolConfirmationHandler handler;

  @MockitoSpyBean
  EmailTools emailTools;

  private void modelWillRequestSendEmailThenSayDone() {
    var toolCallMessage = AssistantMessage.builder()
        .content("")
        .toolCalls(List.of(new AssistantMessage.ToolCall(
            "call-1", "function", "send-email", SEND_EMAIL_ARGS)))
        .build();
    when(chatModel.call(any(Prompt.class))).thenReturn(
        new ChatResponse(List.of(new Generation(toolCallMessage))),
        new ChatResponse(List.of(new Generation(new AssistantMessage("Email sent!")))));
  }

  private ChatResponse askToSendEmail(String sessionId) {
    return chatClient.prompt("Email Alice")
        .advisors(SessionAdvisors.session(sessionId))
        .call().chatResponse();
  }

  @Test
  void approvedToolCallIsExecutedAfterConfirmation() {
    modelWillRequestSendEmailThenSayDone();

    var response = askToSendEmail("approve");
    verify(emailTools, never()).sendEmail(any());

    response = handler.handle(response, new Scanner("y\n"), "approve");

    verify(emailTools).sendEmail(any());
    assertThat(response.getResult().getOutput().getText()).isEqualTo("Email sent!");
  }

  @Test
  void deniedToolCallIsNotExecuted() {
    modelWillRequestSendEmailThenSayDone();

    var response = askToSendEmail("deny");
    response = handler.handle(response, new Scanner("n\n"), "deny");

    verify(emailTools, never()).sendEmail(any());
    assertThat(response.getResult().getOutput().getText()).isEqualTo("Email sent!");
  }

}
