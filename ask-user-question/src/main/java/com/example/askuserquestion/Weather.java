package com.example.askuserquestion;

public record Weather(
    String zipcode,
    String conditions,
    String temperature) {
}
