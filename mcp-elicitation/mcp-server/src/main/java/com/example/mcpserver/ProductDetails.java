package com.example.mcpserver;

public record ProductDetails(
    String sku,
    String description,
    int quantityOnHand,
    float price,
    boolean exclusive) {}
