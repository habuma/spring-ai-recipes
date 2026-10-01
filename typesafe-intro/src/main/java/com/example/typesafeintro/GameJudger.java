package com.example.typesafeintro;

import org.springaicommunity.typesafe.TypeSafeClient;
import org.springaicommunity.typesafe.question.Choice;
import org.springaicommunity.typesafe.question.Noul;
import org.springaicommunity.typesafe.question.Score;
import org.springaicommunity.typesafe.question.SystemOneRequest;
import org.springframework.stereotype.Component;

@Component
public class GameJudger {

  private final TypeSafeClient typeSafeClient;

  public GameJudger(TypeSafeClient typeSafeClient) {
    this.typeSafeClient = typeSafeClient;
  }

  GameJudgement judgeGame(Game game) {
    var response = typeSafeClient.systemOne(
        SystemOneRequest.builder()
            .state(game.details())
            .question("for_casual_gamers",
                Noul.of("Is this game appropriate for casual board gamers?"))
            .question("interaction_style",
                Choice.builder()
                    .instructions("Which best describes the primary player interaction?")
                    .option("Cooperative", "All players work together to achieve some goal")
                    .option("Competitive", "Players work individually to achieve a goal and to thwart each other")
                    .option("Team-based", "Players divided into two or more teams that work together against the other team(s)")
                    .option("Mostly solitaire", "Players work individually with minimal or non-existent interaction with other players")
                    .build())
            .question("learning_difficulty",
                Score.of("How difficult is this game to learn?",
                    "Very easy", "Easy", "Moderate", "Difficult", "Very difficult"))
            .build());

    return new GameJudgement(
        response.noulValue("for_casual_gamers"),
        response.choiceValue("interaction_style"),
        response.scoreValue("learning_difficulty"));
  }

}
