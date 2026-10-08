package com.example.chatloop;

import org.springaicommunity.typesafe.TypeSafeClient;
import org.springaicommunity.typesafe.judge.JevEvaluator;
import org.springaicommunity.typesafe.judge.JevJudge;
import org.springaicommunity.typesafe.question.Noul;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.Evaluator;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class ChatloopApplication {

  public static void main(String[] args) {
    SpringApplication.run(ChatloopApplication.class, args);
  }

  @Bean
  @Profile("!test")
  ApplicationRunner go(
      ChatClient chatClient,
      OpenAiChatModel openAiChatModel,
      TypeSafeClient typeSafeClient) {
    return args -> {
      List<Document> SUPPORTING_DOCUMENTS = List.of(
          Document.builder()
              .text(DemoDocument.DOCUMENT_TEXT).build());

      var claimsDecomposer = new ClaimsDecomposer(openAiChatModel);

      var jevJudge = JevJudge.builder(typeSafeClient)
          .noul("is_grounded", Noul.builder()
              .instructions(
                  "Is every claim in `assistant_answer` supported by `supporting_context`?")
              .whenFalse("Introduces facts the context does not support")
              .build(), 0.7)
          .build();
      var evaluator = new JevEvaluator(jevJudge);

      System.out.println("How can I help?\n");

      try (Scanner scanner = new Scanner(System.in)) {
        while (true) {
          System.out.print("> ");
          if (!scanner.hasNextLine()) break; // to avoid infinite loops in tests
          var input = scanner.nextLine();
          if (input.isBlank()) continue; // allows user to hit return without error

          var answer = chatClient.prompt()
              .system(systemSpec -> systemSpec
                  .text("""
                      You are a helpful assistant answering questions about the document below.
                      Answer in a concise and friendly way.
                      
                      DOCUMENT:
                      {document}
                      """)
                  .param("document", DemoDocument.DOCUMENT_TEXT))
              .user(input)
              .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, "DEMO"))
              .call()
              .content();
          System.out.println("\n - " + answer);

          var claims = claimsDecomposer.decomposeToClaims(input, answer);
          for (String claim : claims.claims()) {
            var isFactual = evaluate(evaluator, input, SUPPORTING_DOCUMENTS, claim);
            if (!isFactual) {
              System.out.println("    ! The claim '" + claim + "' is not supported in the document");
            }
          }

        }
      }
    };
  }

  private boolean evaluate(Evaluator evaluator, String userText, List<Document> dataList, String responseContent) {
    var evaluationRequest = new EvaluationRequest(userText, dataList, responseContent);
    var evaluationResponse = evaluator.evaluate(evaluationRequest);
    return evaluationResponse.isPass();
  }

}
