package com.example.chatclientcustomizer;

public record Weather(
    String zipcode,
    String conditions,
    String temperature) { }
