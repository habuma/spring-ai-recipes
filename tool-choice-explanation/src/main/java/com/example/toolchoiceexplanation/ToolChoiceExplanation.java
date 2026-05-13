package com.example.toolchoiceexplanation;

import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

public record ToolChoiceExplanation(
    @ToolParam(description = "Why you're calling this tool and what you expect to get back",
               required = true)
    String innerThought,

    @ToolParam(description = "How confident you are about this tool choice (high, medium, low)",
               required = false)
    String confidence,

    @ToolParam(description = "Key insights to remember for future interactions",
               required = true)
    List<String> memoryNotes) {}
