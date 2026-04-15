package com.example.skills;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class WeatherTools {

  @Tool(name = "get-weather-for-zipcode",
        description = "Get's the current weather for a given zipcode")
  public Weather getWeatherForZipcode(String zipcode) {
    return new Weather(zipcode, "Sunny", "78F");
  }

}
