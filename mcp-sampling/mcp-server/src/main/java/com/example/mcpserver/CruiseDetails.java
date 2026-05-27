package com.example.mcpserver;

import java.io.StringWriter;

public record CruiseDetails(
    String ship,
    Day[] itinerary) {

  public String toString() {
    var template = """
        CRUISE DETAILS
        
        Ship: %s
        
        Itinerary:
        %s
        """;

    StringWriter stringWriter = new StringWriter();
    for (var day : itinerary) {
      stringWriter.append(day.toString());
    }

    return String.format(template, ship, stringWriter.toString());
  }
}
