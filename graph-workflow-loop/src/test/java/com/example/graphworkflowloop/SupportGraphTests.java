package com.example.graphworkflowloop;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "diagram.output.path=build/test-diagrams")
class SupportGraphTests {

  static final AtomicReference<String> classification = new AtomicReference<>();
  static final AtomicBoolean resolves = new AtomicBoolean();

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
        } else if (system.contains("evaluating a customer support response")) {
          reply = "{\"resolved\": " + resolves.get() + ", \"feedback\": \"needs more detail\"}";
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

  // the app's console runner would block waiting for stdin
  @MockitoBean(name = "go")
  ApplicationRunner consoleRunner;

  @Autowired
  CompiledGraph supportGraph;

  @Autowired
  DiagramBuilder diagramBuilder;

  private RunnableConfig newThread() {
    return RunnableConfig.builder().threadId(java.util.UUID.randomUUID().toString()).build();
  }

  @Test
  void resolvedResponsesEndTheWorkflowAfterOneAttempt() {
    classification.set("billing");
    resolves.set(true);

    var state = supportGraph.invoke(Map.of("user_question", "Help me"), newThread())
        .orElseThrow();

    assertThat(state.data().get("response")).isEqualTo("BILLING-REPLY");
    assertThat(state.data().get("attempts")).isEqualTo(1);
    assertThat(state.data()).doesNotContainKey("escalated");
  }

  @Test
  void unresolvedResponsesAreRetriedThenEscalatedToAHuman() {
    classification.set("technical");
    resolves.set(false);

    var state = supportGraph.invoke(Map.of("user_question", "Help me"), newThread())
        .orElseThrow();

    assertThat(state.data().get("attempts")).isEqualTo(3);
    assertThat(state.data().get("escalated")).isEqualTo(true);
    assertThat((String) state.data().get("finalResponse"))
        .contains("escalated", "TECHNICAL-REPLY", "needs more detail");
  }

  @Test
  void workflowDiagramsCanBeGenerated() throws Exception {
    diagramBuilder.createMermaidDiagram(supportGraph);

    var diagram = java.nio.file.Files.readString(
        java.nio.file.Path.of("build/test-diagrams/support-triage.mmd"));
    assertThat(diagram).contains("classify", "check-resolution");
  }

}
