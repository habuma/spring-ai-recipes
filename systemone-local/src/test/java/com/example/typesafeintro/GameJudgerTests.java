package com.example.typesafeintro;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springaicommunity.typesafe.TypeSafeClient;
import org.springaicommunity.typesafe.question.SystemOneRequest;
import org.springaicommunity.typesafe.response.SystemOneResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GameJudgerTests {

  @Test
  void judgementIsBuiltFromTheTypeSafeAnswers() {
    var client = mock(TypeSafeClient.class);
    var response = mock(SystemOneResponse.class);
    when(response.noulValue("for_casual_gamers")).thenReturn(0.9);
    when(response.choiceValue("interaction_style")).thenReturn("Cooperative");
    when(response.scoreValue("learning_difficulty")).thenReturn(2.0);
    when(client.systemOne(any(SystemOneRequest.class))).thenReturn(response);
    var game = GamesCollection.GAMES[0];

    var judgement = new GameJudger(client).judgeGame(game);

    assertThat(judgement.forCasualGamers()).isEqualTo(0.9);
    assertThat(judgement.interactionStyle()).isEqualTo("Cooperative");
    assertThat(judgement.learningDifficulty()).isEqualTo(2.0);
  }

  @Test
  void requestCarriesTheGameDetailsAndAllThreeQuestions() {
    var client = mock(TypeSafeClient.class);
    var response = mock(SystemOneResponse.class);
    when(client.systemOne(any(SystemOneRequest.class))).thenReturn(response);
    var game = GamesCollection.GAMES[0];

    new GameJudger(client).judgeGame(game);

    var captor = ArgumentCaptor.forClass(SystemOneRequest.class);
    verify(client).systemOne(captor.capture());
    assertThat(captor.getValue().toString()).contains(
        "for_casual_gamers", "interaction_style", "learning_difficulty");
  }

  @Test
  void gameCollectionIsNotEmpty() {
    assertThat(GamesCollection.GAMES).isNotEmpty()
        .allSatisfy(game -> assertThat(game.details()).isNotBlank());
  }

}
