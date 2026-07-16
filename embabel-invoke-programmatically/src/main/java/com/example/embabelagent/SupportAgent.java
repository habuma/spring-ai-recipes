package com.example.embabelagent;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.domain.io.UserInput;

@Agent(description = "Classifies support tickets and routes them")
public class SupportAgent {

  @Action(description = "Derive Ticket from input")
  public ClassificationState deriveTicket(UserInput userInput) {
    return new ClassificationState(new Ticket(userInput.getContent()));
  }

}