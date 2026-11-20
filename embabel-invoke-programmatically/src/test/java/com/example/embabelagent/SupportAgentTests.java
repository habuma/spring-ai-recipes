package com.example.embabelagent;

import com.embabel.agent.core.AgentPlatform;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SupportAgentTests {

  @Autowired
  AgentPlatform agentPlatform;

  @Test
  void supportAgentIsDeployedOnThePlatformWithItsGoals() {
    var agent = agentPlatform.agents().stream()
        .filter(a -> a.getName().contains("SupportAgent"))
        .findFirst().orElseThrow();

    assertThat(agent.getActions()).extracting(a -> a.getName())
        .anyMatch(name -> name.contains("deriveTicket"));
    assertThat(agent.getGoals()).isNotEmpty();
  }

  @Test
  void humanCategoryInputIsParsedLeniently() {
    assertThat(Category.fromHumanInput(" billing ")).isEqualTo(Category.BILLING);
    assertThat(Category.fromHumanInput("Technical")).isEqualTo(Category.TECHNICAL);
    assertThat(Category.fromHumanInput("gibberish")).isEqualTo(Category.UNKNOWN);
  }

}
