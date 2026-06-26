package com.example.embabelagent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

//@Agent(name = "helloAgent",
//    description = "Says hello to someone.")
public class HelloAgent {

  @Action(description = "Derive Person from input")
  public Person derivePerson(UserInput userInput, Ai ai) {
    var prompt = String.format("Derive the person from the input: %s", userInput.getContent());
    return ai
        .withDefaultLlm()
        .createObject(prompt, Person.class);
  }

  @Action(description = "Say hello to someone")
  @AchievesGoal(
      description = "Someone has been greeted with a friendly hello",
      export=@Export(
          name = "sayHello",
          remote = true,
          startingInputTypes = Person.class))
  public Greeting sayHello(Person person) {
    return new Greeting(String.format("Hello, %s!", person.name()));
  }

}
