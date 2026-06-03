package com.example.mcpserver;

public record AirQuality(
    int aqi,
    AirQualityCategory category,
    String primaryPollutant) {}
