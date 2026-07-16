package com.example.embabelagent;

public enum Category {
  BILLING, TECHNICAL, UNKNOWN;

  static Category fromHumanInput(String value) {
    return switch (value.trim().toUpperCase()) {
      case "BILLING" -> BILLING;
      case "TECHNICAL" -> TECHNICAL;
      default -> UNKNOWN;
    };
  }
}
