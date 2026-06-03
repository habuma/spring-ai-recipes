package com.example.mcpserver;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

@Service
public class WeatherTools {

  @McpTool(name = "get-weather-for-zipcode",
      description = "Get weather conditions for a given zipcode")
  public Weather getWeatherForZipCode(
      @McpToolParam(description = "The zipcode to get weather for") String zipcode) {
    return new Weather(zipcode, "Raining cats and dogs", "78F");
  }

  @McpTool(name = "get-air-quality-for-zipcode",
      description = "Get AQI for a given zipcode")
  public AirQuality getAirQualityForZipCode(
      @McpToolParam(description = "The zipcode to get AQI for") String zipcode) {
    return new AirQuality(87, AirQualityCategory.MODERATE, "PM2.5");
  }

  @McpTool(name = "get-allergens-for-zipcode",
      description = "Get current allergy conditions for a given zipcode")
  public AllergyConditions getAllAllergensForZipCode(
      @McpToolParam(description = "The zipcode to get AQI for") String zipcode) {
    return new AllergyConditions(
        AllergyLevel.MODERATE,
        AllergyLevel.MODERATE,
        AllergyLevel.MODERATE,
        AllergyLevel.NONE,
        AllergyLevel.LOW);
  }

}
