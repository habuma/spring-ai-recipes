package com.example.toolconfirmation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EmailTools {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmailTools.class);

  private static final Map<String, String> NAME_TO_EMAIL = Map.of(
      "Alice", "alice@example.com",
      "Bob", "bob@example.com",
      "Charlie", "chuck@example.com"
  );

  @Tool(name = "send-email",
        description = "Sends an email",
        requiresConfirmation = true)
  public String sendEmail(
      @ToolParam(description = "The email to send") Email email) {
    LOGGER.info("Sending email to {}.", email.recipient());
    return "Email sent to " + email.recipient();
  }

  @Tool(name = "get-email-address-for-name",
        description = "Gets an email address for a given name")
  public String getEmailAddressForName(
      @ToolParam(description = "Name of the person to get the email address for") String name) {
    return NAME_TO_EMAIL.get(name);
  }

}
