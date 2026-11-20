package com.example.agui;

import com.agui.community.core.agent.Agent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AgUiWiringTests {

  @MockitoBean
  OpenAiChatModel chatModel;

  @Autowired
  Agent agent;

  @Autowired
  ChatClient.Builder chatClientBuilder;

  @Test
  void springAiAgentIsExposedByTheAgUiStarter() {
    assertThat(agent).isNotNull();
  }

  @Test
  void chatClientBuilderIsCustomizedWithThePirateSystemPrompt() {
    when(chatModel.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
    when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
        List.of(new Generation(new AssistantMessage("Arr!")))));

    assertThat(chatClientBuilder.build().prompt("Hello").call().content())
        .isEqualTo("Arr!");

    var captor = ArgumentCaptor.forClass(Prompt.class);
    verify(chatModel).call(captor.capture());
    assertThat(captor.getValue().getSystemMessage().getText()).contains("pirate");
  }

  @LocalServerPort
  int port;

  @Test
  void agUiEndpointStreamsRunEventsIncludingTheModelsText() throws Exception {
    when(chatModel.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
    var reply = new ChatResponse(List.of(new Generation(new AssistantMessage("Arrr matey"))));
    when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(reply));

    var body = """
        {"threadId":"t1","runId":"r1","state":{},"tools":[],"context":[],
         "forwardedProps":{},
         "messages":[{"id":"m1","role":"user","content":"Hello"}]}""";

    var response = HttpClient.newHttpClient().send(
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/agent"))
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream")
            .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
        HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("RUN_STARTED", "Arrr matey", "RUN_FINISHED");
  }

}
