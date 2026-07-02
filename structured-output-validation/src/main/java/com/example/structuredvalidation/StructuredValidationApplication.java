package com.example.structuredvalidation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StructuredValidationApplication {

  public static void main(String[] args) {
    SpringApplication.run(StructuredValidationApplication.class, args);
  }

  @Bean
  ApplicationRunner applicationRunner(ChatClient.Builder chatClientBuilder) {
    return args -> {
      var chatClient = chatClientBuilder.build();

      var advisor = StructuredOutputValidationAdvisor.builder()
          .outputType(TopSongs.class)
          .maxRepeatAttempts(5) // <-- Set max retries
          .build();

      var topSongs = chatClient.prompt()
          .user("""
              What were the top 10 songs on the Billboard Year-End Hot 100 
              singles of 1985?
              """)
          .advisors(advisor)
          .call()
          .entity(TopSongs.class,
              spec -> spec
                  .validateSchema()
                  .useProviderStructuredOutput());

      System.out.println(topSongs);
    };
  }

}
