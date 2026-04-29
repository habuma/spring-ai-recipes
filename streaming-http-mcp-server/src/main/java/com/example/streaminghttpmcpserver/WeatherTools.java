package com.example.streaminghttpmcpserver;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

@Service
public class WeatherTools {

  @McpTool(name = "get-weather-for-zipcode", description = "")
  public Weather getWeatherForZipcode(
      @McpToolParam(description = "The zipcode to get weather for") String zipcode) {
    return new Weather(zipcode, "Raining cats and dogs", "78F");
  }

}
