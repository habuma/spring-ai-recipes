package com.example.springbootdocsingestor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.ai.document.Document;

public class LuceneDocumentWriter implements AutoCloseable {

  private static final String ID_FIELD = "id";
  private static final String CONTENT_FIELD = "content";
  private static final String METADATA_FIELD = "metadata";

  private final Analyzer analyzer;
  private final Directory directory;
  private final IndexWriter writer;

  private final ObjectMapper objectMapper;

  public LuceneDocumentWriter(Path indexPath) throws IOException {
    this.analyzer = new StandardAnalyzer();
    this.directory = FSDirectory.open(indexPath);

    var config = new IndexWriterConfig(analyzer);
    config.setSimilarity(new BM25Similarity());

    this.writer = new IndexWriter(directory, config);
    this.objectMapper = new ObjectMapper();
  }

  public void add(List<Document> documents) throws IOException {
    for (Document document : documents) {
      var luceneDocument = new org.apache.lucene.document.Document();

      luceneDocument.add(
          new StringField(ID_FIELD, document.getId(), Field.Store.YES));
      luceneDocument.add(
          new TextField(CONTENT_FIELD, document.getText(), Field.Store.YES));
      luceneDocument.add(
          new StoredField(METADATA_FIELD, objectMapper.writeValueAsString(document.getMetadata())));

      writer.addDocument(luceneDocument);
    }

    writer.commit();
  }

  @Override
  public void close() throws IOException {
    writer.close();
    directory.close();
    analyzer.close();
  }
}