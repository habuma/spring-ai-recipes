package com.example.embabelagent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.core.hitl.WaitFor;
import com.embabel.agent.domain.io.UserInput;

@Agent(description = "Classifies and responds to customer support tickets")
public class SupportAgent {

  @Action(description = "Derive Ticket from input")
  public Ticket deriveTicket(UserInput userInput) {
    return new Ticket(userInput.getContent());
  }

  @Action
  RoutedTicket classify(Ticket ticket, Ai ai) {
    var classification = ai.withDefaultLlm()
        .createObject("""
                        Classify this support ticket as BILLING, TECHNICAL, or UNKNOWN.

                        Use UNKNOWN only if the ticket could reasonably be either billing or technical.

                        Ticket:
                        %s
                        """.formatted(ticket.text()),
            Classification.class);

    return switch (classification.category()) {
      case BILLING -> new BillingTicket(ticket);
      case TECHNICAL -> new TechnicalTicket(ticket);
      case UNKNOWN -> new UnclearTicket(ticket, classification.reason());
    };
  }

  @Action
  HumanClassification askHuman(UnclearTicket ticket) {
    return WaitFor.formSubmission("""
                        I could not confidently classify this support ticket.

                        Ticket:
                        %s

                        Reason:
                        %s

                        Please choose BILLING or TECHNICAL.
                        """.formatted(ticket.ticket().text(), ticket.reason()),
        HumanClassification.class);
  }

  @Action(clearBlackboard = true)
  RoutedTicket routeHumanClassification(
      UnclearTicket ticket,
      HumanClassification humanClassification) {

    var category = humanClassification.category();

    return switch (category) {
      case BILLING -> new BillingTicket(ticket.ticket());
      case TECHNICAL -> new TechnicalTicket(ticket.ticket());
      case UNKNOWN -> new UnclearTicket(
          ticket.ticket(),
          """
          '%s' is not a valid category. Please enter BILLING or TECHNICAL.
          """.formatted(humanClassification.categoryString()));
    };
  }

  @AchievesGoal(description = "Answer a billing support ticket")
  @Action
  SupportResponse answerBillingTicket(BillingTicket ticket, Ai ai) {
    return ai.withDefaultLlm()
        .createObject("""
                        Write a helpful customer-support response for this billing issue.

                        Ticket:
                        %s
                        """.formatted(ticket.ticket().text()),
            SupportResponse.class);
  }

  @AchievesGoal(description = "Answer a technical support ticket")
  @Action
  SupportResponse answerTechnicalTicket(TechnicalTicket ticket, Ai ai) {
    return ai.withDefaultLlm()
        .createObject("""
                        Write a helpful customer-support response for this technical issue.

                        Ticket:
                        %s
                        """.formatted(ticket.ticket().text()),
            SupportResponse.class);
  }

}