package com.example.toolconfirmation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallDecision;
import org.springframework.ai.model.tool.ToolCallDecisions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.session.advisor.SessionAdvisors;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ToolConfirmationHandler {

  private final ChatClient chatClient;
  private final ToolCallingManager toolCallingManager;
  private final ToolCallingChatOptions options;

  public ToolConfirmationHandler(
      ChatClient chatClient,
      ToolCallingManager toolCallingManager,
      ToolCallback[] toolCallbacks) {
    this.chatClient = chatClient;
    this.toolCallingManager = toolCallingManager;
    this.options = ToolCallingChatOptions.builder()
        .toolCallbacks(toolCallbacks)
        .build();
  }

  public ChatResponse handle(ChatResponse response, Scanner scanner, String sessionId) {

    // Keep processing until the model no longer requests tool confirmation.
    while (!ToolCallDecisions.awaitingDecision(response).isEmpty()) {

      // Ask the user to approve or deny pending tool calls.
      var decisions = confirmToolCalls(response, scanner);

      // Execute approved calls; denied calls receive a denial response.
      var result = toolCallingManager.executeToolCalls(
          new Prompt(List.of(), options), response, decisions);

      var history = result.conversationHistory();

      // Resume the conversation with the tool results.
      // The model may request additional tools requiring confirmation.
      response = chatClient.prompt()
          .messages(history.getLast())
          .advisors(SessionAdvisors.session(sessionId))
          .call()
          .chatResponse();
    }

    return response;
  }

  private ToolCallDecisions confirmToolCalls(
      ChatResponse response, Scanner scanner) {

    var assistantMessage = response.getResult().getOutput();

    // Identify which tool calls require confirmation and their unique keys.
    var awaiting = ToolCallDecisions.awaitingDecision(response);
    var toolCalls = assistantMessage.getToolCalls();
    var keys = ToolCallDecisions.toolCallKeys(assistantMessage);

    List<ToolCallDecision> decisions = new ArrayList<>();
    for (int i = 0; i < toolCalls.size(); i++) {
      var toolCall = toolCalls.get(i);
      var key = keys.get(i);

      // Calls not requiring confirmation are approved automatically.
      boolean approved = true;

      if (awaiting.contains(key)) {
        System.out.println("\nTool: " + toolCall.name());
        System.out.println("Arguments: " + toolCall.arguments());
        System.out.print("\nAllow? (y/N): ");

        if (!scanner.hasNextLine()) {
          throw new IllegalStateException("Input closed during confirmation");
        }

        // Only an explicit "y" approves the call; anything else denies it.
        approved = scanner.nextLine().trim().equalsIgnoreCase("y");
      }

      // Every tool call needs a decision, even if confirmation wasn't required.
      decisions.add(approved
          ? ToolCallDecision.allow(key)
          : ToolCallDecision.deny(key));
    }

    return ToolCallDecisions.of(decisions.toArray(ToolCallDecision[]::new));
  }

}