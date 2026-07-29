package com.example.essentialrag;

import com.integrallis.vectors.core.SimilarityFunction;
import com.integrallis.vectors.db.VectorCollection;
import com.integrallis.vectors.spring.ai.JavaVectorsVectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.nio.file.Path;

@Configuration
public class RagIngestionConfig {

  private static final Logger logger = LoggerFactory.getLogger(RagIngestionConfig.class);

  @Value("${rag.document.url}")
  String documentUrl;

  @Bean(destroyMethod = "close")
  VectorCollection collection(EmbeddingModel embeddingModel) {
    return VectorCollection.builder()
        .dimension(embeddingModel.dimensions())
        .metric(SimilarityFunction.COSINE)
        .storagePath(Path.of("/tmp/vectors/rag-recipe"))
        .build();
  }

  @Bean
  VectorStore vectorStore(EmbeddingModel embeddingModel, VectorCollection collection) {
    return JavaVectorsVectorStore.builder(embeddingModel, collection)
        .build();
  }

  @Bean
  @Order(-1)
  ApplicationRunner load(VectorStore vectorStore) {
    return args -> {
      logger.info("Loading document from {}.",documentUrl);

      var reader = new TikaDocumentReader(documentUrl);
      var splitter = TokenTextSplitter.builder().build();

      vectorStore.accept(
          splitter.apply(
              reader.get()));

      logger.info("Document loading complete.");
    };
  }

}
