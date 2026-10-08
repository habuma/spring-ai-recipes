package com.example.chatloop;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springaicommunity.typesafe.TypeSafeClient;
import org.springaicommunity.typesafe.judge.JevEvaluator;
import org.springaicommunity.typesafe.judge.JevJudge;
import org.springaicommunity.typesafe.question.Noul;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.Evaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class FactCheckingEvaluatorLearningTests {

  @Autowired
  TypeSafeClient typeSafeClient;

  @Autowired
  ChatModel chatModel;


  private static List<Document> SUPPORTING_DOCUMENTS;

  @BeforeAll
  static void beforeAll() {
    SUPPORTING_DOCUMENTS = List.of(
        Document.builder()
            .text(DemoDocument.DOCUMENT_TEXT).build());
  }

  @Test
  void systemOneEvaluation() {
    var jevJudge = JevJudge.builder(typeSafeClient)
        .noul("is_grounded", Noul.builder()
            .instructions(
                "Is every claim in `assistant_answer` supported by `supporting_context`?")
            .whenFalse("Introduces facts the context does not support")
            .build(), 0.85d)
        .build();
    var evaluator = new JevEvaluator(jevJudge);

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
  void evaluateThreeDecomposedClaims() {
    var jevJudge = JevJudge.builder(typeSafeClient)
        .noul("is_grounded", Noul.builder()
            .instructions(
                "Is every claim in `assistant_answer` supported by `supporting_context`?")
            .whenFalse("Introduces facts the context does not support")
            .build(), 0.85d)
        .build();
    var evaluator = new JevEvaluator(jevJudge);

    assertThat(evaluate(evaluator,
        "Will there be a Spring AI workshop?",
        SUPPORTING_DOCUMENTS,
        "Details and session schedules are available at https://www.arcofai.com/.")).isFalse();

    assertThat(evaluate(evaluator,
        "Will there be a Spring AI workshop?",
        SUPPORTING_DOCUMENTS,
        "The website https://www.arcofai.com/ contains details and session schedules about Arc of AI.")).isFalse();

    assertThat(evaluate(evaluator,
        "Will there be a Spring AI workshop?",
        SUPPORTING_DOCUMENTS,
        "Details and session schedules for Arc of AI are available at https://www.arcofai.com/.")).isFalse();
  }

  @Test
  void decomposedClaimsSpringAiWorkshopEvaluation() {
    var claimsDecomposer = new ClaimsDecomposer(chatModel);
    var jevJudge = JevJudge.builder(typeSafeClient)
        .noul("is_grounded", Noul.builder()
            .instructions(
                "Is every claim in `assistant_answer` supported by `supporting_context`?")
            .whenFalse("Introduces facts the context does not support")
            .build(), 0.80d)
        .build();
    var evaluator = new JevEvaluator(jevJudge);

    var question = "Will there be a Spring AI workshop?";
    var answer = """
        Yes. Spring AI is listed as a topic at Arc of AI and will be covered
        in presentations and workshops. For details and session schedules,
        see https://www.arcofai.com/.
        """;

    var claims = claimsDecomposer.decomposeToClaims(question, answer);
    boolean[] expectedFactualities = { true, true, false };

    int i = 0;
    for(String claim : claims.claims()) {
      var isFactual = evaluate(evaluator, question, SUPPORTING_DOCUMENTS, claim);
      System.out.println(" - " + isFactual + "  ::  " + claim);
      assertThat(isFactual).isEqualTo(expectedFactualities[i++]);
    }
  }

  private boolean evaluate(Evaluator evaluator, String userText, List<Document> dataList, String responseContent) {
    var evaluationRequest = new EvaluationRequest(userText, dataList, responseContent);
    var evaluationResponse = evaluator.evaluate(evaluationRequest);
    return evaluationResponse.isPass();
  }

}
