package com.example.embabelagent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.State;
import com.embabel.agent.api.common.Ai;

@State
public record TechnicalSupportState(Ticket ticket) implements SupportState {

  @Action
  @AchievesGoal(description = "Respond to a technical support ticket")
  SupportResponse respond(Ai ai) {
    return ai.withDefaultLlm().createObject("""
            Write a helpful customer-support response for this technical issue.
            Prefix the response with "TECHNICAL: "

            Ticket:
            %s
        """.formatted(ticket.text()), SupportResponse.class);
  }

}
