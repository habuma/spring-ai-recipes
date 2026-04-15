package com.example.skills;

public record Weather(
    String zipcode,
    String conditions,
    String temperature) {}
