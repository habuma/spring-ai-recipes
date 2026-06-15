package com.example.voicechat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class VoiceChatApplication {

  public static void main(String[] args) {
    SpringApplication.run(VoiceChatApplication.class, args);
  }

  @Bean
  ApplicationRunner go(ChatClient chatClient, AudioRecorder recorder, Transcriber transcriber) {
    return args -> {
      System.out.println("How can I help?\n");

      try (Scanner scanner = new Scanner(System.in)) {
        while (true) {
          System.out.print("> ");
          if (!scanner.hasNextLine()) break; // to avoid infinite loops in tests
          var input = scanner.nextLine();
          if (input.isBlank()) continue; // allows user to hit return without error

          if (input.equalsIgnoreCase("/record")) {
            recorder.start();
            continue;
          }

          if (input.equalsIgnoreCase("/stop")) {
            byte[] audio = recorder.stop();
            input = transcriber.transcribe(audio);
            System.out.println("> " + input);
          }

          System.out.println("\n - " +
              chatClient.prompt(input)
                  .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, "DEMO"))
                  .call()
                  .content());
        }
      }
    };
  }

}
