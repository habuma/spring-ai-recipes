package com.example.mcpserver;

public record AllergyConditions(
    AllergyLevel overall,
    AllergyLevel treePollen,
    AllergyLevel grassPollen,
    AllergyLevel weedPollen,
    AllergyLevel mold) {}
