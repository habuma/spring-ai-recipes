package com.example.a2aserver;

public record Weather(
    String zipcode,
    String conditions,
    String temperature) {
}
