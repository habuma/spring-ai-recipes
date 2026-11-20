package com.example.a2aserver;

import org.a2aproject.sdk.spec.AgentCard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.Mockito;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class A2aServerTests {

  @TestConfiguration
  static class StubModel {
    @Bean
    @Primary
    OpenAiChatModel stubChatModel() {
      var model = Mockito.mock(OpenAiChatModel.class);
      when(model.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
      when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(
          List.of(new Generation(new AssistantMessage("Sunny and 75F")))));
      return model;
    }
  }

  @LocalServerPort
  int port;

  @Autowired
  AgentCard agentCard;

  @Test
  void weatherToolReturnsWeatherForTheZipcode() {
    var weather = new WeatherTools().getWeatherForZipcode("81657");

    assertThat(weather.zipcode()).isEqualTo("81657");
    assertThat(weather.conditions()).isNotBlank();
  }

  @Test
  void agentCardDescribesTheWeatherAgent() {
    assertThat(agentCard.name()).isEqualTo("My Weather Agent");
    assertThat(agentCard.skills()).extracting(s -> s.id()).contains("my_agent");
    assertThat(agentCard.capabilities().streaming()).isFalse();
  }

  @Test
  void agentCardIsPublishedAtTheWellKnownLocation() throws Exception {
    var response = HttpClient.newHttpClient().send(
        HttpRequest.newBuilder(URI.create(
            "http://localhost:" + port + "/a2a/.well-known/agent-card.json")).GET().build(),
        HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("My Weather Agent");
    assertThat(response.body()).contains("supportedInterfaces").contains("JSONRPC")
        .contains("/a2a/");
  }

  @Test
  void messageSendIsAnsweredByTheChatClient() throws Exception {
    var request = """
        {"jsonrpc":"2.0","id":"1","method":"message/send","params":{
          "message":{"kind":"message","role":"user","messageId":"m-1",
            "parts":[{"kind":"text","text":"What is the weather?"}]}}}""";

    var response = HttpClient.newHttpClient().send(
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/a2a/"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(request)).build(),
        HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("Sunny and 75F");
  }

}
