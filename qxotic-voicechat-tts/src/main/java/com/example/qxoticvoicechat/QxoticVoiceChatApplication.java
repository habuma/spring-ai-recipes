package com.example.qxoticvoicechat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.session.advisor.SessionAdvisors;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Scanner;

@SpringBootApplication
@EnableAsync
public class QxoticVoiceChatApplication {

  public static void main(String[] args) {
    SpringApplication.run(QxoticVoiceChatApplication.class, args);
  }

  @Bean
  ApplicationRunner go(ChatClient chatClient, AudioRecorder recorder, Transcriber transcriber, SpeechService speechService) {
    return args -> {
      System.out.println("How can I help?\n");

      try (Scanner scanner = new Scanner(System.in)) {
        while (true) {
          System.out.print("> ");
          if (!scanner.hasNextLine()) break; // to avoid infinite loops in tests
          var input = scanner.nextLine();
          if (input.isBlank()) continue; // allows user to hit return without error

          if (input.equalsIgnoreCase("/record")) {
            System.out.println("Speak your question...");
            recorder.start();
            continue;
          }

          if (input.equalsIgnoreCase("/stop")) {
            byte[] audio = recorder.stop();
            input = transcriber.transcribe(audio);
            System.out.println("> " + input);
          }

          var answer = chatClient.prompt(input)
              .advisors(SessionAdvisors.session("DEMO"))
              .call()
              .content();
          System.out.println("\n - " + answer);
          speechService.speak(answer);
        }
      }
    };
  }

}
