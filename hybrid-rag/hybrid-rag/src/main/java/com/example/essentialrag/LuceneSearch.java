package com.example.essentialrag;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.ai.document.Document;

public class LuceneSearch implements AutoCloseable {

  private static final String ID_FIELD = "id";
  private static final String CONTENT_FIELD = "content";
  private static final String METADATA_FIELD = "metadata";

  private final Analyzer analyzer;
  private final Directory directory;
  private final DirectoryReader reader;
  private final IndexSearcher searcher;
  private final ObjectMapper objectMapper;

  public LuceneSearch(Path indexPath) throws IOException {
    this.analyzer = new StandardAnalyzer();
    this.directory = FSDirectory.open(indexPath);
    this.reader = DirectoryReader.open(directory);
    this.searcher = new IndexSearcher(reader);

    searcher.setSimilarity(new BM25Similarity());
    
    this.objectMapper = new ObjectMapper();
  }

  public List<Document> search(String queryText, int topK) {
    try {
      var parser = new QueryParser(CONTENT_FIELD, analyzer);
      var query = parser.parse(
          QueryParser.escape(queryText));

      var hits = searcher.search(query, topK);
      var storedFields = searcher.storedFields();

      var results = new ArrayList<Document>();
      for (var hit : hits.scoreDocs) {
        var luceneDocument =
            storedFields.document(hit.doc);

        var metadata = objectMapper.readValue(
            luceneDocument.get(METADATA_FIELD),
            new TypeReference<Map<String, Object>>() {
            });

        results.add(new Document(
            luceneDocument.get(ID_FIELD),
            luceneDocument.get(CONTENT_FIELD),
            metadata));
      }

      return results;
    } catch (IOException | ParseException e) {
      throw new IllegalStateException("Failed to search Lucene index", e);
    }
  }

  @Override
  public void close() throws IOException {
    reader.close();
    directory.close();
    analyzer.close();
  }
}