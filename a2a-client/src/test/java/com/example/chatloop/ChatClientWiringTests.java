package com.example.chatloop;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies the ChatClient wiring (advisors, tools, system prompt) end to end
 * against a mocked model, so no API key or network access is needed.
 */
@SpringBootTest
class ChatClientWiringTests {

  // The ChatClient builder resolves the remote agent's card from this fixed URL
  // at startup, so a stub A2A server is started on port 8080 for the test.
  static HttpServer stubAgent;

  static final String AGENT_CARD = """
      {"name":"Weather Agent","description":"Provides weather information",
       "url":"http://localhost:8080/a2a/","version":"1.0.0",
       "capabilities":{"streaming":false},
       "defaultInputModes":["text"],"defaultOutputModes":["text"],
       "skills":[{"id":"weather","name":"Weather","description":"Weather","tags":["weather"]}],
       "protocolVersion":"0.3.0"}
      """;

  @BeforeAll
  static void startStubAgent() throws IOException {
    try {
      stubAgent = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
    } catch (IOException e) {
      Assumptions.abort("Port 8080 is in use; can't stub the A2A agent");
    }
    stubAgent.createContext("/a2a/.well-known", exchange -> {
      var body = AGENT_CARD.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, body.length);
      exchange.getResponseBody().write(body);
      exchange.close();
    });
    stubAgent.start();
  }

  @AfterAll
  static void stopStubAgent() {
    if (stubAgent != null) stubAgent.stop(0);
  }

  @MockitoBean
  OpenAiChatModel chatModel;

  @Autowired
  ChatClient chatClient;

  @Test
  void chatClientIsWiredWithConfiguredAdvisorsAndTools() {
    when(chatModel.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
    when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
        List.of(new Generation(new AssistantMessage("Mocked reply")))));

    assertThat(chatClient.prompt("Hello")
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "test"))
        .call().content()).isEqualTo("Mocked reply");
    assertThat(chatClient.prompt("Again")
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "test"))
        .call().content()).isEqualTo("Mocked reply");

    var captor = ArgumentCaptor.forClass(Prompt.class);
    verify(chatModel, atLeast(2)).call(captor.capture());
    var calls = captor.getAllValues();
    var first = calls.get(calls.size() - 2);
    var second = calls.get(calls.size() - 1);

    assertThat(((ToolCallingChatOptions) first.getOptions()).getToolCallbacks())
        .extracting(cb -> cb.getToolDefinition().name())
        .contains("Task");

    // the second call carries the first exchange from chat memory
    assertThat(second.getInstructions())
        .extracting(m -> m.getMessageType())
        .contains(MessageType.USER, MessageType.ASSISTANT);
    assertThat(second.getContents()).contains("Hello");
  }

}
