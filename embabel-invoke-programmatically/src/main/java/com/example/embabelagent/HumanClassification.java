package com.example.embabelagent;

public record HumanClassification(String categoryString) {
  Category category() {
    return Category.fromHumanInput(categoryString);
  }
}
