package com.example.mcpserver;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Service;

@Service
public class SlowTools {

  @McpTool(name = "get-weather-for-zipcode",
      description = "Get weather conditions for a given zipcode")
  public Weather getWeatherForZipCode(
      @McpToolParam(description = "The zipcode to get weather for") String zipcode,
      McpSyncRequestContext requestContext) throws Exception {

    // simulate a slow response with a periodic progress update every 10%
    for(int i = 0; i < 10; i++) {
      int percentage = (i + 1) * 10;
      requestContext.progress(
          progressSpec -> progressSpec.progress(percentage));
      Thread.sleep(2000);
    }

    return new Weather(zipcode, "Raining cats and dogs", "78F");
  }

}
