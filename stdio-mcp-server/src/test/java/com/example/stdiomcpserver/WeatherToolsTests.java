package com.example.stdiomcpserver;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherToolsTests {

  @Test
  void returnsWeatherForZipcode() {
    var weather = new WeatherTools().getWeatherForZipcode("81657");

    assertThat(weather.zipcode()).isEqualTo("81657");
    assertThat(weather.conditions()).isNotBlank();
    assertThat(weather.temperature()).isNotBlank();
  }

}
