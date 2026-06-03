package com.example.mcpserver;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AllergyLevel {
  VERY_HIGH("Very High"),
  HIGH("High"),
  MODERATE("Moderate"),
  LOW("Low"),
  NONE("None");

  private final String label;

  AllergyLevel(String label) {
    this.label = label;
  }

  @JsonValue
  public String getLabel() {
    return label;
  }

}
