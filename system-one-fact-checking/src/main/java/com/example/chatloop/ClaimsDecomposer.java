package com.example.chatloop;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;

import java.util.List;

public class ClaimsDecomposer {

  private static final String DECOMPOSITION_PROMPT_TEMPLATE = """
        Extract all factual claims made by the answer.

        Each claim must:
        - Be a single, standalone declarative statement.
        - Express only one factual assertion.
        - Include enough context to be understood without the question.
        - Preserve the meaning of the answer.
        - Not add facts or make inferences beyond what the answer states.
        - Omit advice, suggestions, and other non-factual statements.
        """;

  private static final String DECOMPOSITION_INPUT_TEMPLATE = """
      Question:
      {question}
      
      Answer:
      {answer}
      """;

  private final ChatClient.Builder chatClientBuilder;

  public ClaimsDecomposer(ChatModel chatModel) {
    this.chatClientBuilder = ChatClient.builder(chatModel);
  }

  public Claims decomposeToClaims(String question, String answer) {

    var chatClient = this.chatClientBuilder.build();
    return chatClient.prompt()
        .system(DECOMPOSITION_PROMPT_TEMPLATE)
        .user(userSpec -> userSpec
            .text(DECOMPOSITION_INPUT_TEMPLATE)
            .param("question", question)
            .param("answer", answer))
        .options(ChatOptions.builder()
            .temperature(0.0)
            .model("gpt-4o-mini"))
        .call()
        .entity(Claims.class);
  }

  public record Claims(List<String> claims) {}

}
