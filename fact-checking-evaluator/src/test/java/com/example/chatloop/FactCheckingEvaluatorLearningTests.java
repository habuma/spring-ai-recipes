package com.example.chatloop;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.Evaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class FactCheckingEvaluatorLearningTests {

  @Autowired
  ChatModel openAiChatModel;

  @Autowired
  @Qualifier("minicheck")
  ChatClient.Builder miniCheckChatClientBuilder;

  private static List<Document> SUPPORTING_DOCUMENTS;

  @BeforeAll
  static void beforeAll() {
    SUPPORTING_DOCUMENTS = List.of(
        Document.builder()
            .text(DemoDocument.DOCUMENT_TEXT).build());
  }

  @Test
  void openAiEvaluation() {
    var chatClientBuilder = ChatClient.builder(openAiChatModel);
    var evaluator = FactCheckingEvaluator.builder(chatClientBuilder).build();

    assertThat(evaluate(
        evaluator,
        "When is Arc of AI 2027?",
        SUPPORTING_DOCUMENTS,
        "Arc of AI 2027 takes place April 19–22, 2027 (in Austin, Texas)."))
        .isTrue();

    assertThat(evaluate(
        evaluator,
        "Where is Arc of AI 2027?",
        SUPPORTING_DOCUMENTS,
        "Arc of AI 2027 takes place April 19–22, 2027 (in Dallas, Texas)."))
        .isFalse();
  }

  @Test
  @Disabled("Won't pass for reasons described in the recipe")
  void openAiSpringAiWorkshopEvaluation() {
    var chatClientBuilder = ChatClient.builder(openAiChatModel);
    var evaluator = FactCheckingEvaluator.builder(chatClientBuilder).build();

    assertThat(evaluate(
        evaluator,
        "Will there be a Spring AI workshop?",
        SUPPORTING_DOCUMENTS,
        """
        Yes. Spring AI is listed as a topic at Arc of AI and will be covered
        in presentations and workshops. For details and session schedules, 
        see https://www.arcofai.com/.
        """))
        .isFalse();
  }


  @Test
  void minicheckEvaluation() {
    var evaluator = FactCheckingEvaluator.forBespokeMinicheck(miniCheckChatClientBuilder);

    assertThat(evaluate(
        evaluator,
        "When is Arc of AI 2027?",
        SUPPORTING_DOCUMENTS,
        "Arc of AI 2027 takes place April 19–22, 2027 (in Austin, Texas)."))
        .isTrue();

    assertThat(evaluate(
        evaluator,
        "Where is Arc of AI 2027?",
        SUPPORTING_DOCUMENTS,
        "Arc of AI 2027 takes place April 19–22, 2027 (in Dallas, Texas)."))
        .isFalse();
  }

  @Test
  void minicheckEvaluation_shouldPass() {
    var evaluator = FactCheckingEvaluator.forBespokeMinicheck(miniCheckChatClientBuilder);

    assertThat(evaluate(
        evaluator,
        "Will there be a Spring AI workshop?",
        SUPPORTING_DOCUMENTS,
        "Yes. There will be a Spring AI workshop."))
        .isFalse();
  }

  @Test
  @Disabled("Won't pass for reasons described in the recipe")
  void miniCheckSpringAiWorkshopEvaluation() {
    var evaluator = FactCheckingEvaluator.forBespokeMinicheck(miniCheckChatClientBuilder);

    assertThat(evaluate(
        evaluator,
        "Will there be a Spring AI workshop?",
        SUPPORTING_DOCUMENTS,
        """
        Yes. Spring AI is listed as a topic at Arc of AI and will be covered
        in presentations and workshops. For details and session schedules, 
        see https://www.arcofai.com/.
        """))
        .isFalse();
  }

  private boolean evaluate(Evaluator evaluator, String userText, List<Document> dataList, String responseContent) {
    var evaluationRequest = new EvaluationRequest(userText, dataList, responseContent);
    var evaluationResponse = evaluator.evaluate(evaluationRequest);
    return evaluationResponse.isPass();
  }

}
