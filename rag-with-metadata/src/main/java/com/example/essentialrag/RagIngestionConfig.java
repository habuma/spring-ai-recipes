package com.example.essentialrag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;

@Configuration
public class RagIngestionConfig {

  private static final Logger logger = LoggerFactory.getLogger(RagIngestionConfig.class);

  @Value("${rag.documents}")
  Resource[] documentResources;

  @Bean
  @Order(-1)
  ApplicationRunner load(VectorStore vectorStore) {
    return args -> {
      for(Resource documentResource : documentResources) {
        var filename = documentResource.getFilename();
        logger.info("Loading document from {}.", filename);

        var reader = new TikaDocumentReader(documentResource);
        var splitter = TokenTextSplitter.builder().build();

        var titleTag = filename.substring(0, filename.lastIndexOf('.'));

        vectorStore.accept(
            splitter.apply(
                reader.get().stream()
                    .peek(document ->
                      document.getMetadata().put("title", titleTag)
                    )
                    .toList()
            ));
      }

      logger.info("Document loading complete.");
    };
  }

}
