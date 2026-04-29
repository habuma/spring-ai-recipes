package com.example.streaminghttpmcpserver;

public record Weather(
    String zipcode,
    String conditions,
    String temperature) {}
