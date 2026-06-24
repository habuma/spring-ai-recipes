package com.example.embabelagent;

public record UnclearTicket(
    Ticket ticket,
    String reason
) implements RoutedTicket {}
