package com.example.embabelagent;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.State;
import com.embabel.agent.core.hitl.WaitFor;

@State
public record NeedsHumanClassificationState(
    Ticket ticket,
    String reason)
    implements SupportState {

  @Action
  HumanClassification askHuman() {
    return WaitFor.formSubmission("""
        I could not confidently classify this support ticket.
        Ticket:
        %s
        Reason:
        %s
        Please choose BILLING or TECHNICAL.
        """.formatted(ticket.text(), reason),
        HumanClassification.class);
  }

  @Action(clearBlackboard = true)
  SupportState routeHumanClassification(
      HumanClassification humanClassification) {
    return switch (humanClassification.category()) {
      case BILLING -> new BillingSupportState(ticket);
      case TECHNICAL -> new TechnicalSupportState(ticket);
      case UNKNOWN -> new NeedsHumanClassificationState(
          ticket,
          "Please enter either BILLING or TECHNICAL.");
    };
  }

}
