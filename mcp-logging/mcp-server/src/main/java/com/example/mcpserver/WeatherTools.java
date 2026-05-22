package com.example.mcpserver;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Service;

@Service
public class WeatherTools {

  @McpTool(name = "get-weather-for-zipcode", description = "")
  public Weather getWeatherForZipcode(
      @McpToolParam(description = "The zipcode to get weather for") String zipcode,
      McpSyncRequestContext requestContext) {

    requestContext.info("Finding weather conditions for " + zipcode);

    return new Weather(zipcode, "Raining cats and dogs", "78F");
  }

}
