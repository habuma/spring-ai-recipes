package com.example.toolconfirmation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailToolsTests {

  private final EmailTools tools = new EmailTools();

  @Test
  void looksUpEmailAddressesByName() {
    assertThat(tools.getEmailAddressForName("Alice")).isEqualTo("alice@example.com");
    assertThat(tools.getEmailAddressForName("Nobody")).isNull();
  }

  @Test
  void sendsEmail() {
    var result = tools.sendEmail(new Email("bob@example.com", "Hi", "Hello"));

    assertThat(result).isEqualTo("Email sent to bob@example.com");
  }

}
