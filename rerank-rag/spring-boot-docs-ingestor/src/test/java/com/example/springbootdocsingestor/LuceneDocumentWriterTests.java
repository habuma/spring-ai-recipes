package com.example.springbootdocsingestor;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.document.Document;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LuceneDocumentWriterTests {

  @TempDir
  Path indexPath;

  @Test
  void writtenDocumentsAreStoredWithIdContentAndMetadata() throws Exception {
    try (var writer = new LuceneDocumentWriter(indexPath)) {
      writer.add(List.of(
          new Document("doc-1", "Actuator endpoints", Map.of("filename", "a.adoc")),
          new Document("doc-2", "Profiles", Map.of("filename", "b.adoc"))));
    }

    try (var directory = FSDirectory.open(indexPath);
         var reader = DirectoryReader.open(directory)) {
      assertThat(reader.numDocs()).isEqualTo(2);
      var stored = reader.storedFields().document(0);
      assertThat(stored.get("id")).isEqualTo("doc-1");
      assertThat(stored.get("content")).isEqualTo("Actuator endpoints");
      assertThat(stored.get("metadata")).contains("a.adoc");
    }
  }

}
