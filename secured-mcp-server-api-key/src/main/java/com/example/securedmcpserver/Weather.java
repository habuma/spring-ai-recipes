package com.example.securedmcpserver;

public record Weather(
    String zipcode,
    String conditions,
    String temperature) {}
