package com.example.essentialrag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class TitleHelper {

  private final ChatClient chatClient;

  public TitleHelper(ChatModel chatModel) {
    this.chatClient = ChatClient.builder(chatModel).build();
  }

  public String determineGameTitle(String question) {
    var title = chatClient.prompt()
        .user(userSpec -> userSpec
            .text("""
                  Your job is to try to determine the title of a game from
                  the question asked.
                  
                  The game choices are:
                  - camp-bowwow
                  - frog-panic
                  - taco-truck
                  - unknown
                  
                  If the game's title isn't explicitly mentioned in the question,
                  or you don't recognize the game's title, then say  "unknown".
                  
                  The question is:
                  {question}
                  """)
            .param("question", question))
        .call()
        .content();


    return title.equals("unknown") ? null : title;
  }

}
