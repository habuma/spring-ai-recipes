package com.example.chatloop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class WeatherTools {

  private static final Logger logger = LoggerFactory.getLogger(WeatherTools.class);

  @Tool(name = "get-temperature-for-zipcode",
        description = "Get the temperature for a given zipcode")
  public Weather getWeatherForZipcode(
      @ToolParam(description = "The zipcode to get the weather for") String zipcode) {

    logger.info("Getting weather for {}", zipcode);

    return new Weather(zipcode, "Raining cats and dogs", 78);
  }

}
