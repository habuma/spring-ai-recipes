package com.example.logging;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class WeatherTools {

  @Tool(name = "get-weather-for-zipcode",
      description = "Get weather for a given zipcode")
  public Weather getWeatherForZipcode(
      @ToolParam(description = "The zipcode to get the weather for") String zipcode) {
    return new Weather(zipcode, "Raining cats and dogs", "81.5F");
  }

}
