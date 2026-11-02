package com.example.chatloop;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.part.UnknownPart;
import org.springframework.ai.openai.responses.OpenAiResponsesMetadata;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;

@SpringBootApplication
public class ChatloopApplication {

  public static void main(String[] args) {
    SpringApplication.run(ChatloopApplication.class, args);
  }

  @Bean
  ApplicationRunner go(ChatClient.Builder chatClientBuilder) {
    return args -> {

      var chatClient = chatClientBuilder.build();

      var task = """
          Use Python to simulate rolling five six-sided dice
          100,000 times. Report the most common total and
          the probability of rolling at least 25.
          """;

      var chatResponse = chatClient.prompt()
          .user(task)
          .call()
          .chatResponse();
      var answer = chatResponse.getResult().getOutput().getText();

      System.out.println(answer);

      var toolCalls = chatResponse.getResult()
          .getMetadata()
          .get(OpenAiResponsesMetadata.HOSTED_TOOL_CALLS);

      System.out.println("Hosted tool calls: " + toolCalls);

      var mapper = new ObjectMapper();
      chatResponse.getResult().getOutput().getParts().stream()
          .filter(part -> part instanceof UnknownPart)
          .map(part -> (UnknownPart) part)
          .filter(part -> "code_interpreter_call".equals(part.kind()))
          .map(part -> {
              return mapper.readTree(part.rawJson()).path("code").asText();
          })
          .forEach(System.out::println);
    };
  }

}
