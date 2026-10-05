package com.example.typesafeintro;

import org.springaicommunity.typesafe.TypeSafeClient;
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
  ApplicationRunner go(GameJudger gameJudger) {
    return args -> {
      for (Game game : GamesCollection.GAMES) {
        var judgment = gameJudger.judgeGame(game);
        System.out.println(" - " + game.title());
        System.out.println("   For casual gamers  ? " + judgment.forCasualGamers());
        System.out.println("   Interaction style  : " + judgment.interactionStyle());
        System.out.println("   Learning difficulty: " + judgment.learningDifficulty());
        System.out.println();
      }
    };
  }

}
