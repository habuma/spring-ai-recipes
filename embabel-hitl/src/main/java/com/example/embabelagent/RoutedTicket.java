package com.example.embabelagent;

public sealed interface RoutedTicket
    permits BillingTicket, TechnicalTicket, UnclearTicket {
}
