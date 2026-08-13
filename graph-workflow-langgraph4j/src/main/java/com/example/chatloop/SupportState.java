package com.example.chatloop;

import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

public class SupportState extends AgentState {

  public static final String USER_QUESTION = "user_question";

  private static final String CATEGORY = "category";

  private static final String RESOLUTION = "resolution";

  public SupportState(Map<String, Object> initialState) {
    super(initialState);
  }

  public String getUserQuestion() {
    return (String) value(USER_QUESTION).orElse("");
  }

  public String getCategory() {
    return (String) value(CATEGORY).orElse("");
  }

  public String getResolution() {
    return (String) value(RESOLUTION).orElse("");
  }

}
