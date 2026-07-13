package com.example.safeguardadvisor;

public record GuardrailVerdict(
    boolean allowed,
    String reason,
    String violatedRule
) {}