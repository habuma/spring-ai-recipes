package com.example.chatloop;

public record Weather(
    String zipcode,
    String conditions,
    int temperatureInFahrenheit) {}
