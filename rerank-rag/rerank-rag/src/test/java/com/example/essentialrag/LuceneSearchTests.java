package com.example.essentialrag;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LuceneSearchTests {

  @TempDir
  Path indexPath;

  private void index(String id, String content) throws Exception {
    try (var analyzer = new StandardAnalyzer();
         var directory = FSDirectory.open(indexPath);
         var writer = new IndexWriter(directory,
             new IndexWriterConfig(analyzer).setSimilarity(new BM25Similarity()))) {
      var doc = new org.apache.lucene.document.Document();
      doc.add(new StringField("id", id, Field.Store.YES));
      doc.add(new TextField("content", content, Field.Store.YES));
      doc.add(new StoredField("metadata", "{\"source\":\"" + id + ".adoc\"}"));
      writer.addDocument(doc);
    }
  }

  @Test
  void findsDocumentsByKeywordAndRestoresMetadata() throws Exception {
    index("one", "Actuator endpoints expose health information");
    // second writer call appends to the same index
    try (var analyzer = new StandardAnalyzer();
         var directory = FSDirectory.open(indexPath);
         var writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
      var doc = new org.apache.lucene.document.Document();
      doc.add(new StringField("id", "two", Field.Store.YES));
      doc.add(new TextField("content", "Profiles select configuration per environment", Field.Store.YES));
      doc.add(new StoredField("metadata", "{\"source\":\"two.adoc\"}"));
      writer.addDocument(doc);
    }

    try (var search = new LuceneSearch(indexPath)) {
      var results = search.search("actuator health", 5);

      assertThat(results).hasSize(1);
      assertThat(results.get(0).getId()).isEqualTo("one");
      assertThat(results.get(0).getText()).contains("Actuator");
      assertThat(results.get(0).getMetadata()).containsEntry("source", "one.adoc");
    }
  }

  @Test
  void queriesWithSpecialCharactersDoNotFail() throws Exception {
    index("one", "Actuator endpoints expose health information");

    try (var search = new LuceneSearch(indexPath)) {
      assertThat(search.search("what is (actuator)? [health] AND/OR *", 5)).isNotNull();
    }
  }

}
