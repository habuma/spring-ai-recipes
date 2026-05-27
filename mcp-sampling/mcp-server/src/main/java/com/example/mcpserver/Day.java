package com.example.mcpserver;

public record Day(
    String date,
    String description) {

  public String toString() {
    return date + "  ::: " +  description;
  }

}
