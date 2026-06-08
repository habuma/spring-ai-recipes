package com.example.semanticcaching;

import io.qdrant.client.QdrantClient;
import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.ai.vectorstore.redis.cache.semantic.DefaultSemanticCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SemanticCacheConfig {

  @Bean
  VectorStore cacheVectorStore(
      QdrantClient qdrantClient,
      EmbeddingModel embeddingModel) {
    return QdrantVectorStore.builder(qdrantClient, embeddingModel)
        .collectionName("semantic-cache")
        .initializeSchema(true)
        .build();
  }

  @Bean
  SemanticCache semanticCache(
      @Qualifier("cacheVectorStore") VectorStore vectorStore,
      EmbeddingModel embeddingModel,
      @Value("${semantic.cache.similarity-threshold:0.8}")
      double threshold) {
    return DefaultSemanticCache.builder()
        .vectorStore(vectorStore)
        .embeddingModel(embeddingModel)
        .similarityThreshold(threshold)
        .build();
  }

  @Bean
  SemanticCacheAdvisor semanticCacheAdvisor(SemanticCache semanticCache) {
    return SemanticCacheAdvisor.builder()
        .cache(semanticCache)
        .build();
  }

}
