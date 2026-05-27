package com.example.mcpserver;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Service;

@Service
public class CruiseTools {

  private final CruiseRepository cruiseRepository;

  public CruiseTools(CruiseRepository cruiseRepository) {
    this.cruiseRepository = cruiseRepository;
  }

  @McpTool(name = "get-cruise-writeup",
           description = "Get a traveler-friendly description for a cruise.")
  public String getItineraryWriteup(
      @McpToolParam(description = "The ID of the cruise to get the writeup for")
      String cruiseId,
      McpSyncRequestContext requestContext) {

    if (requestContext.sampleEnabled()) {
      var itinerary = cruiseRepository.getCruiseDetails(cruiseId);

      String systemPromptTemplate = """
          Write a traveler-friendly description for the cruise below. Where 
          appropriate, mention amenities that may also be enjoyed on the ship.
        
          The cruise details are as follows:
        
          """;

      var result = requestContext.sample(spec ->
          spec.systemPrompt(systemPromptTemplate + itinerary)
              .modelPreferences(
                  mp -> mp.modelHint("gpt-4o-mini")));
      return result.content().toString();
    } else {
      return "Unable to produce cruise writeup.";
    }
  }

}
