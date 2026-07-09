package com.example.safeguardadvisor;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OutputSafeGuardAdvisor implements CallAdvisor {

  private final List<String> sensitiveWords;
  private final String failureResponse;
  private final int order;

  private OutputSafeGuardAdvisor(List<String> sensitiveWords, String failureResponse, int order) {
    this.sensitiveWords = sensitiveWords;
    this.failureResponse = failureResponse;
    this.order = order;
  }

  public static Builder builder() {
    return new Builder();
  }

  @NotNull
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public int getOrder() {
    return order;
  }

  @Override
  @NullMarked
  public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
    ChatClientResponse response = chain.nextCall(request);
    var chatResponse = response.chatResponse();
    if (chatResponse == null || chatResponse.getResults().isEmpty()) {
      return response;
    }

    var results = chatResponse.getResults();
    var filteredResults = results.stream()
        .map(generation -> {
          String content = generation.getOutput().getText();
          if (containsSensitiveWord(content)) {
            var filteredMessage = new AssistantMessage(failureResponse);
            var metadata = generation.getMetadata();
            return new Generation(filteredMessage, metadata);
          }
          return generation;
        })
        .collect(Collectors.toList());

    if (!filteredResults.equals(results)) {
      var filteredChatResponse = new ChatResponse(filteredResults, chatResponse.getMetadata());
      return response.mutate().chatResponse(filteredChatResponse).build();
    }

    return response;
  }

  private boolean containsSensitiveWord(String text) {
    if (!StringUtils.hasText(text)) return false;
    return sensitiveWords.stream().anyMatch(text::contains);
  }

  //
  // BUILDER
  //
  public static class Builder {
    private List<String> sensitiveWords = List.of();
    private String failureResponse = "Response contained forbidden content.";
    private int order = Ordered.LOWEST_PRECEDENCE;

    public Builder sensitiveWords(String... sensitiveWords) {
      this.sensitiveWords = Arrays.asList(sensitiveWords);
      return this;
    }

    public Builder sensitiveWords(List<String> sensitiveWords) {
      this.sensitiveWords = sensitiveWords;
      return this;
    }

    public Builder failureResponse(String failureResponse) {
      this.failureResponse = failureResponse;
      return this;
    }

    public Builder order(int order) {
      this.order = order;
      return this;
    }

    public OutputSafeGuardAdvisor build() {
      return new OutputSafeGuardAdvisor(sensitiveWords, failureResponse, order);
    }
  }
}
