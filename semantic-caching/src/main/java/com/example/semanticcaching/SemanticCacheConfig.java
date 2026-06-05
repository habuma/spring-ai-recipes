package com.example.semanticcaching;

import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.cache.semantic.DefaultSemanticCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class SemanticCacheConfig {

  @Bean
  JedisPooled jedisPooled(
      @Value("${spring.data.redis.host:localhost}") String host,
      @Value("${spring.data.redis.port:6379}") int port) {
    return new JedisPooled(host, port);
  }

  @Bean
  SemanticCache semanticCache(
      JedisPooled jedisPooled,
      EmbeddingModel embeddingModel,
      @Value("${semantic.cache.similarity-threshold:0.9}") double threshold) {
    return DefaultSemanticCache.builder()
        .jedisClient(jedisPooled)
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
