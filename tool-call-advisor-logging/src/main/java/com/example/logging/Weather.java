package com.example.logging;

public record Weather(
    String zipcode,
    String conditions,
    String temperature) {
}
