package com.example.essentialrag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.util.Assert;

public class HydeQueryTransformer implements QueryTransformer {

  private static final String DEFAULT_PROMPT = """
            Write a short passage that would appear in a document that answers
            the following question.

            Do not mention that the passage is hypothetical.
            Do not explain your reasoning.
            Do not ask follow-up questions.
            Use terminology that a relevant technical document would likely use.

            Question:
            {query}

            Hypothetical document:
            """;

  private final ChatClient chatClient;
  private final PromptTemplate promptTemplate;

  public HydeQueryTransformer(
      ChatClient.Builder chatClientBuilder,
      PromptTemplate promptTemplate) {

    Assert.notNull(chatClientBuilder,
        "chatClientBuilder must not be null");
    Assert.notNull(promptTemplate,
        "promptTemplate must not be null");

    this.chatClient = chatClientBuilder.build();
    this.promptTemplate = promptTemplate;
  }

  @Override
  public Query transform(Query query) {
    Assert.notNull(query, "query must not be null");
    Assert.hasText(query.text(), "query text must not be empty");

    String hypotheticalDocument = this.chatClient.prompt()
        .user(user -> user
            .text(this.promptTemplate.getTemplate())
            .param("query", query.text()))
        .call()
        .content();

    Assert.hasText(
        hypotheticalDocument,
        "The model did not generate a hypothetical document");

    return query.mutate()
        .text(hypotheticalDocument)
        .build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private ChatClient.Builder chatClientBuilder;

    private PromptTemplate promptTemplate =
        new PromptTemplate(DEFAULT_PROMPT);

    public Builder chatClientBuilder(
        ChatClient.Builder chatClientBuilder) {

      this.chatClientBuilder = chatClientBuilder;
      return this;
    }

    public Builder promptTemplate(
        PromptTemplate promptTemplate) {

      this.promptTemplate = promptTemplate;
      return this;
    }

    public HydeQueryTransformer build() {
      return new HydeQueryTransformer(
          this.chatClientBuilder,
          this.promptTemplate);
    }
  }
}
