package com.example.toolconfirmation;

public record Email(
    String recipient,
    String subject,
    String body) {}
