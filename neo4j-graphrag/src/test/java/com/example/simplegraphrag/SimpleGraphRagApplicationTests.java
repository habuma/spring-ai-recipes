package com.example.simplegraphrag;

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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = "spring.docker.compose.enabled=false")
class SimpleGraphRagApplicationTests {

  @Container
  static final GenericContainer<?> neo4j = new GenericContainer<>("neo4j:2026.10.0")
      .withEnv("NEO4J_AUTH", "neo4j/testpassword")
      .withExposedPorts(7687)
      .waitingFor(Wait.forLogMessage(".*Started\\..*", 1)
          .withStartupTimeout(Duration.ofMinutes(2)));

  @DynamicPropertySource
  static void neo4jProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.neo4j.uri",
        () -> "bolt://" + neo4j.getHost() + ":" + neo4j.getMappedPort(7687));
    registry.add("spring.neo4j.authentication.username", () -> "neo4j");
    registry.add("spring.neo4j.authentication.password", () -> "testpassword");
  }

  // stands in for the LLM that extracts relationships from text
  @TestConfiguration
  static class StubModel {
    @Bean
    @Primary
    OpenAiChatModel stubChatModel() {
      var model = Mockito.mock(OpenAiChatModel.class);
      when(model.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
      when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(
          new Generation(new AssistantMessage("""
              {"edges":[
                {"from":"Sagrada","relation":"DESIGNED_BY","to":"Daryl Andrews"},
                {"from":"Sagrada","relation":"PUBLISHED_BY","to":"Floodgate Games"}]}
              """)))));
      return model;
    }
  }

  @Autowired
  KnowledgeGraphService service;

  @Test
  void extractedRelationshipsArePersistedAndQueryable() {
    service.addToKnowledgeGraph("Sagrada was designed by Daryl Andrews and published by Floodgate Games.");

    assertThat(service.findRelationships("Sagrada", Relation.DESIGNED_BY, null))
        .containsExactly(new Edge("Sagrada", Relation.DESIGNED_BY, "Daryl Andrews"));
    assertThat(service.findRelationships(null, Relation.PUBLISHED_BY, "Floodgate Games"))
        .containsExactly(new Edge("Sagrada", Relation.PUBLISHED_BY, "Floodgate Games"));
    assertThat(service.findRelationships(null, Relation.DESIGNED_BY, null)).isEmpty();
  }

}
