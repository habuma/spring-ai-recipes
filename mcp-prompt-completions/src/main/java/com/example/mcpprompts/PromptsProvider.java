package com.example.mcpprompts;

import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpComplete;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptsProvider {

  @McpPrompt(name = "summarize-text",
      description = "Summarize text")
  public String summarizeText(String text) {

    var rawPrompt = """
        Summarize the given text.
        
        If the text is short (a handful of paragraphs), the summary should be no
        longer than 5 sentences.
        
        If the text is divided into sections, write a 1-2 sentence summary of
        each section.
        
        If the text is lengthy but not broken into sections, the summary should be
        3-5 paragraphs at most.
        
        TEXT TO SUMMARIZE:
        %s
        """;

    return String.format(rawPrompt, text);
  }


  private static final List<String> CITIES = List.of(
      "New York", "Los Angeles", "Chicago", "Houston", "Phoenix",
      "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose",
      "Austin", "Jacksonville", "Fort Worth", "Columbus", "Charlotte",
      "San Francisco", "Indianapolis", "Seattle", "Denver", "Washington"
  );

  @McpComplete(prompt = "get-weather-for-city")
  public List<String> completeCityName(String prefix) {
    return CITIES.stream()
        .filter(city -> city.toLowerCase().startsWith(prefix.toLowerCase()))
        .limit(10)
        .toList();
  }

  @McpPrompt(name = "get-weather-for-city",
      description = "Get the weather for a given city")
  public String weatherForCityPrompt(
      @McpArg(name = "city-name",
          description = "City's name",
          required = true) String cityName) {

    return String.format("What is the weather in %s.", cityName);
  }

}
