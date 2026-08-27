package com.example.springbootdocsingestor;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Configuration
public class RagIngestionConfig {

  @Value("${spring.boot.project.path}")
  private String projectPath;

  @Bean
  LuceneDocumentWriter luceneDocumentWriter(
      @Value("${lucene.index.path}")Path luceneIndexPath) throws IOException {
    return new LuceneDocumentWriter(luceneIndexPath);
  }

  @Bean
  ApplicationRunner loadSpringBootDocumentation(
      VectorStore vectorStore, LuceneDocumentWriter luceneDocumentWriter) {
    return args -> {
      Path docsRoot = Path.of(
          projectPath,
          "documentation/spring-boot-docs/src/docs/antora/modules/reference/pages");

      var splitter = TokenTextSplitter.builder()
          .withChunkSize(800)
          .build();

      List<Document> documents;

      try (Stream<Path> files = Files.walk(docsRoot)) {

        documents = files
            .filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".adoc"))
            .flatMap(path -> {
              var reader =
                  new TikaDocumentReader(new FileSystemResource(path));

              return reader.read().stream()
                  .peek(document -> {
                    document.getMetadata().put(
                        "source",
                        docsRoot.relativize(path).toString());

                    document.getMetadata().put(
                        "filename",
                        path.getFileName().toString());
                  });
            })
            .toList();
      }

      List<Document> chunks = splitter.apply(documents);

      vectorStore.add(chunks);
      luceneDocumentWriter.add(chunks);

      System.out.printf(
          "Loaded %d documents as %d chunks%n",
          documents.size(),
          chunks.size());
    };
  }

}
