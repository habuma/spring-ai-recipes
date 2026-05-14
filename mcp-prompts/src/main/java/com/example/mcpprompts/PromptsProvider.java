package com.example.mcpprompts;

import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Service;

@Service
public class PromptsProvider {

  @McpPrompt(name = "summarize-text",
      description = "Summarize text")
  public String summarizeText(String text) {

    var rawPrompt = """
        Summarize the given text.
        
        If the text is short (a handful of paragraphs), the summary should be no
        longer than 5 sentences.
        
        If the text is divided into sections, write a 1-2 sentence summary of
        each section.
        
        If the text is lengthy but not broken into sections, the summary should be
        3-5 paragraphs at most.
        
        TEXT TO SUMMARIZE:
        %s
        """;

    return String.format(rawPrompt, text);
  }

}
