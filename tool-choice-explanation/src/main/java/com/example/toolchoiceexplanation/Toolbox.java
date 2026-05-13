package com.example.toolchoiceexplanation;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class Toolbox {

  @Tool(description = "Get the current weather for a given location")
  public String weather(String location) {
    return "The current weather in " + location + " is rainy with a temperature of 12°C.";
  }

  @Tool(description = "Gets the current date and time")
  public String getCurrentDateAndTime() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  @Tool(description = "Finds the number of players that can play a given game")
  public Integer players(String gameTitle) {
    return 6;
  }

  @Tool(description = "Finds the current wait time for an attraction in Disneyland in minutes")
  public Integer getWaitTime(String attractionName) {
    return 20;
  }

}
