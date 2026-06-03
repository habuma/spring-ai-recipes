package com.example.mcpserver;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AirQualityCategory {

  GOOD("Good"),
  MODERATE("Moderate"),
  UNHEALTHY_FOR_SENSITIVE_GROUPS("Unhealthy for Sensitive Groups"),
  UNHEALTHY("Unhealthy"),
  VERY_UNHEALTHY("Very Unhealthy"),
  HAZARDOUS("Hazardous");

  private final String label;

  AirQualityCategory(String label) {
    this.label = label;
  }

  @JsonValue
  public String getLabel() {
    return label;
  }
}
