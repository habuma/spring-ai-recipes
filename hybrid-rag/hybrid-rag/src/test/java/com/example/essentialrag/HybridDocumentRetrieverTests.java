package com.example.essentialrag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HybridDocumentRetrieverTests {

  private Document doc(String id) {
    return new Document(id, "text of " + id, java.util.Map.of());
  }

  @Test
  void documentsFoundByBothSearchesRankAboveDocumentsFoundByOne() {
    var vectorStore = mock(VectorStore.class);
    var lucene = mock(LuceneSearch.class);
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(List.of(doc("a"), doc("b"), doc("c")));
    when(lucene.search(eq("question"), anyInt()))
        .thenReturn(List.of(doc("d"), doc("c"), doc("e")));

    var results = new HybridDocumentRetriever(vectorStore, lucene)
        .retrieve(new Query("question"));

    assertThat(results).extracting(Document::getId)
        .containsExactlyInAnyOrder("a", "b", "c", "d", "e");
    // "c" appears in both result lists, so it is fused to the top
    assertThat(results.get(0).getId()).isEqualTo("c");
  }

  @Test
  void resultsAreLimitedToTen() {
    var vectorStore = mock(VectorStore.class);
    var lucene = mock(LuceneSearch.class);
    when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(
        java.util.stream.IntStream.range(0, 10).mapToObj(i -> doc("v" + i)).toList());
    when(lucene.search(eq("q"), anyInt())).thenReturn(
        java.util.stream.IntStream.range(0, 10).mapToObj(i -> doc("l" + i)).toList());

    assertThat(new HybridDocumentRetriever(vectorStore, lucene)
        .retrieve(new Query("q"))).hasSize(10);
  }

}
