package com.example.embabelagent;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.State;
import com.embabel.agent.api.common.Ai;

@State
public record ClassificationState(Ticket ticket) {

  @Action
  SupportState classify(Ai ai) {
    var classification = ai.withDefaultLlm().createObject(""" 
              Classify this support ticket as BILLING, TECHNICAL, or UNKNOWN.
              
              Use UNKNOWN only if the ticket could reasonably be either billing or technical.

              Ticket:
              %s""".formatted(ticket.text()), Classification.class);
    return switch (classification.category()) {
      case BILLING -> new BillingSupportState(ticket);
      case TECHNICAL -> new TechnicalSupportState(ticket);
      case UNKNOWN -> new NeedsHumanClassificationState(ticket, classification.reason());
    };
  }

}
