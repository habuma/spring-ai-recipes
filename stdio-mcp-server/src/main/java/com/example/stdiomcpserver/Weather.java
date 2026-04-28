package com.example.stdiomcpserver;

public record Weather(
    String zipcode,
    String conditions,
    String temperature) {}
