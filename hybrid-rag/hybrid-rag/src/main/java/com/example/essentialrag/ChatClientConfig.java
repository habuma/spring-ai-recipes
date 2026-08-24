package com.example.essentialrag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Configuration
public class ChatClientConfig {

  @Bean
  @Primary
  ChatClient.Builder openAiChatClientBuilder(
      OpenAiChatModel openAiChatModel,
      ObjectProvider<ChatClientBuilderCustomizer> customizers) {

    return applyCustomizers(
        ChatClient.builder(openAiChatModel),
        customizers
    );
  }

  @Bean
  @Qualifier("queryCompressionChatClientBuilder")
  ChatClient.Builder compressionChatClientBuilder(
      OpenAiChatModel openAiChatModel) {
    return ChatClient.builder(openAiChatModel)
        .defaultOptions(OpenAiChatOptions
            .builder().model("gpt-5-nano"));
  }

  private ChatClient.Builder applyCustomizers(
      ChatClient.Builder builder,
      ObjectProvider<ChatClientBuilderCustomizer> customizers) {

    customizers.orderedStream()
        .forEach(customizer -> customizer.customize(builder));

    return builder;
  }

  @Bean
  ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
    return chatClientBuilder.build();
  }

  @Bean
  ChatClientBuilderCustomizer chatMemoryCustomizer() {
    return builder -> {
      builder.defaultAdvisors(
          MessageChatMemoryAdvisor.builder(
                  MessageWindowChatMemory.builder()
                      .maxMessages(500)
                      .build())
              .build());
    };
  }

  @Bean
  @Order(1)
  QueryTransformer queryTransformer(
      @Qualifier("queryCompressionChatClientBuilder") ChatClient.Builder chatClientBuilder) {
    return CompressionQueryTransformer.builder()
        .chatClientBuilder(chatClientBuilder)
        .build();
  }

  @Bean
  @Order(2)
  QueryTransformer hydeQueryTransformer(
      @Qualifier("queryCompressionChatClientBuilder") ChatClient.Builder chatClientBuilder) {
    return HydeQueryTransformer.builder()
        .chatClientBuilder(chatClientBuilder)
        .build();
  }

  @Bean
  LuceneSearch luceneSearch(
      @Value("${lucene.index.path}")Path luceneIndexPath) throws IOException {
    return new LuceneSearch(luceneIndexPath);
  }

  @Bean
  HybridDocumentRetriever hybridDocumentRetriever(VectorStore vectorStore, LuceneSearch luceneSearch) {
    return new HybridDocumentRetriever(vectorStore, luceneSearch);
  }

  @Bean
  ChatClientBuilderCustomizer addRagAdvisor(
      QueryTransformer[] queryTransformers,
      HybridDocumentRetriever hybridDocumentRetriever) {
    return builder -> {
      var ragAdvisor = RetrievalAugmentationAdvisor.builder()
          .queryTransformers(queryTransformers)
          .documentRetriever(hybridDocumentRetriever)
          .documentJoiner(documentsForQuery ->
              documentsForQuery.values().stream()
                  .flatMap(List::stream)
                  .flatMap(List::stream)
                  .toList())
          .build();

      builder.defaultAdvisors(ragAdvisor);
    };
  }

}
