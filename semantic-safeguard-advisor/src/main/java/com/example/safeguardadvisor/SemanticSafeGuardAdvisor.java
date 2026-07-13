package com.example.safeguardadvisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Map;

public class SemanticSafeGuardAdvisor implements CallAdvisor {

  private static final Logger log = LoggerFactory.getLogger(SemanticSafeGuardAdvisor.class);

  private final SemanticGuardrailJudge judge;
  private final boolean checkInput;
  private final boolean checkOutput;
  private final String failureResponse;
  private final int order;

  private SemanticSafeGuardAdvisor(
      SemanticGuardrailJudge judge,
      boolean checkInput,
      boolean checkOutput,
      String failureResponse,
      int order) {

    this.judge = judge;
    this.checkInput = checkInput;
    this.checkOutput = checkOutput;
    this.failureResponse = failureResponse;
    this.order = order;
  }

  public static SemanticSafeGuardAdvisor.Builder builder() {
    return new SemanticSafeGuardAdvisor.Builder();
  }

  @Override
  public String getName() {
    return "semantic-safe-guard-advisor";
  }

  @Override
  public int getOrder() {
    return order;
  }

  @Override
  public ChatClientResponse adviseCall(
      ChatClientRequest request,
      CallAdvisorChain chain) {

    if (checkInput) {
      String inputText = extractInputText(request);

      GuardrailVerdict verdict = judge.judge(inputText, "user input");

      if (!verdict.allowed()) {
        log.error(String.format("Input text '%s' is not allowed.\nReason: %s\nViolated rule: %s",
            inputText, verdict.reason(), verdict.violatedRule()));
        return createFailureResponse(request);
      }
    }

    ChatClientResponse response = chain.nextCall(request);
    var chatResponse = response.chatResponse();


    if (checkOutput) {
      String outputText = chatResponse
          .getResult()
          .getOutput()
          .getText();

      GuardrailVerdict verdict = judge.judge(outputText, "model output");

      if (!verdict.allowed()) {
        log.error(String.format("Output text '%s' is not allowed.\nReason: %s\nViolated rule: %s",
            outputText, verdict.reason(), verdict.violatedRule()));
        return createFailureResponse(request);
      }
    }

    return response;
  }

  private ChatClientResponse createFailureResponse(ChatClientRequest chatClientRequest) {
    return ChatClientResponse.builder()
        .chatResponse(ChatResponse.builder()
            .generations(List.of(new Generation(new AssistantMessage(this.failureResponse))))
            .build())
        .context(Map.copyOf(chatClientRequest.context()))
        .build();
  }

  private String extractInputText(ChatClientRequest request) {
    return request.prompt()
        .getUserMessage()
        .getText();
  }

  //
  // BUILDER
  //
  public static class Builder {
    private SemanticGuardrailJudge judge;
    private boolean checkInput = true;
    private boolean checkOutput = true;
    private String failureResponse = "Forbidden content";
    private int order = Ordered.LOWEST_PRECEDENCE;

    public SemanticSafeGuardAdvisor.Builder judge(SemanticGuardrailJudge judge) {
      this.judge = judge;
      return this;
    }

    public SemanticSafeGuardAdvisor.Builder failureResponse(String failureResponse) {
      this.failureResponse = failureResponse;
      return this;
    }

    public SemanticSafeGuardAdvisor.Builder order(int order) {
      this.order = order;
      return this;
    }

    public SemanticSafeGuardAdvisor.Builder checkInput(boolean checkInput) {
      this.checkInput = checkInput;
      return this;
    }

    public SemanticSafeGuardAdvisor.Builder checkOutput(boolean checkOutput) {
      this.checkOutput = checkOutput;
      return this;
    }

    public SemanticSafeGuardAdvisor build() {
      return new SemanticSafeGuardAdvisor(judge, checkInput, checkOutput, failureResponse, order);
    }
  }
}
