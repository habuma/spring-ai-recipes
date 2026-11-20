package com.example.graphworkflow;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class SupportGraphTests {

  static final AtomicReference<String> classification = new AtomicReference<>();

  // scripted model: answers the classifier and each support node differently
  @TestConfiguration
  static class ScriptedModel {
    @Bean
    @Primary
    OpenAiChatModel scriptedChatModel() {
      var model = Mockito.mock(OpenAiChatModel.class);
      when(model.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
      when(model.call(any(Prompt.class))).thenAnswer(invocation -> {
        var system = ((Prompt) invocation.getArgument(0))
            .getSystemMessage().getText();
        String reply;
        if (system.contains("Classify the customer support request")) {
          reply = classification.get();
        } else if (system.contains("billing support assistant")) {
          reply = "BILLING-REPLY";
        } else {
          reply = "TECHNICAL-REPLY";
        }
        return new ChatResponse(List.of(new Generation(new AssistantMessage(reply))));
      });
      return model;
    }
  }

  @Autowired
  CompiledGraph supportGraph;

  private String run(String category) {
    classification.set(category);
    var state = supportGraph.invoke(Map.of("user_question", "Help me")).orElseThrow();
    return state.value("response", String.class).orElseThrow();
  }

  @Test
  void billingQuestionsAreRoutedToTheBillingNode() {
    assertThat(run("billing")).isEqualTo("BILLING-REPLY");
  }

  @Test
  void technicalQuestionsAreRoutedToTheTechnicalNode() {
    assertThat(run("technical")).isEqualTo("TECHNICAL-REPLY");
  }

  @Test
  void unrecognizedCategoriesDefaultToTechnical() {
    assertThat(run("something else")).isEqualTo("TECHNICAL-REPLY");
  }

}
