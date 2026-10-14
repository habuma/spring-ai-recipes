package com.example.streaminghttpmcpserver;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.MetaProvider;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WeatherTools {

  @McpTool(
      name = "get-weather-for-zipcode",
      description = "Gets weather for a given zipcode",
      metaProvider = AppsMetaProvider.class)
  public Weather getWeatherForZipcode(
      @McpToolParam(description = "The zipcode to get weather for") String zipcode) {
    return new Weather(zipcode, "Raining cats and dogs", "78F");
  }

  public static final class AppsMetaProvider implements MetaProvider {
    @Override
    public Map<String, Object> getMeta() {
      return Map.of("ui",
          Map.of(
              "resourceUri", "ui://weather/weather.html"),
          "ui/resourceUri", "ui://weather/weather.html"
      );
    }
  }

}
