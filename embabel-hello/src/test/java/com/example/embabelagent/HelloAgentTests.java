package com.example.embabelagent;

import com.embabel.agent.core.AgentPlatform;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HelloAgentTests {

  @Autowired
  AgentPlatform agentPlatform;

  @Test
  void helloAgentIsDeployedOnThePlatformWithItsActionsAndGoal() {
    var agent = agentPlatform.agents().stream()
        .filter(a -> a.getName().equals("helloAgent"))
        .findFirst().orElseThrow();

    assertThat(agent.getActions()).extracting(a -> a.getName())
        .anyMatch(name -> name.contains("derivePerson"))
        .anyMatch(name -> name.contains("sayHello"));
    assertThat(agent.getGoals()).isNotEmpty();
  }

  @Test
  void greetsThePersonByName() {
    var greeting = new HelloAgent().sayHello(new Person("Craig"));

    assertThat(greeting.greetingText()).isEqualTo("Hello, Craig!");
  }

}
