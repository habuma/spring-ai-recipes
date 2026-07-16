package com.example.embabelagent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.State;
import com.embabel.agent.api.common.Ai;

@State
public record BillingSupportState(Ticket ticket) implements SupportState {

  @Action
  @AchievesGoal(description = "Respond to a billing support ticket")
  SupportResponse respond(Ai ai) {
    return ai.withDefaultLlm().createObject("""
            Write a helpful customer-support response for this billing issue.

            Ticket:
            %s
        """.formatted(ticket.text()),
        SupportResponse.class);
  }

}
