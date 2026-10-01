package com.example.typesafeintro;

import org.springaicommunity.typesafe.TypeSafeClient;
import org.springaicommunity.typesafe.question.Choice;
import org.springaicommunity.typesafe.question.Noul;
import org.springaicommunity.typesafe.question.Score;
import org.springaicommunity.typesafe.question.SystemOneRequest;
import org.springaicommunity.typesafe.response.SystemOneResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TypesafeIntroApplication {

  public static void main(String[] args) {
    SpringApplication.run(TypesafeIntroApplication.class, args);
  }

  @Bean
  TypeSafeClient typeSafeClient(@Value("${TYPESAFEAI_API_KEY}") String apiKey) {
    return TypeSafeClient.builder()
        .apiKey(apiKey)
        .build();
  }

  @Bean
  ApplicationRunner go(GameJudger gameJudger) {
    return args -> {
      var startTime = System.nanoTime();
      for (Game game : GamesCollection.GAMES) {
        var judgment = gameJudger.judgeGame(game);
        System.out.println(" - " + game.title());
        System.out.println("   For casual gamers  ? " + judgment.forCasualGamers());
        System.out.println("   Interaction style  : " + judgment.interactionStyle());
        System.out.println("   Learning difficulty: " + judgment.learningDifficulty());
        System.out.println();
      }

      var endTime = System.nanoTime();
      var totalTime = (endTime - startTime);
      System.err.println("Game judgement in " + totalTime + " nanoseconds");
      System.err.println(" In milliseconds: " + totalTime / 1000000);
    };
  }

}
