package com.example.chatloop;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameTools {

  @Tool(description = """
    Find board games suitable for a group.
    Returns games with player counts, play times,
    complexity, and whether they are cooperative.
    """)
  public List<Game> findGames() {
    return List.of(
        new Game("Pandemic", 2, 4, 45, true, 2.4),
        new Game("Forbidden Island", 2, 4, 30, true, 1.7),
        new Game("Just One", 3, 7, 20, true, 1.0),
        new Game("Codenames", 2, 8, 15, false, 1.3),
        new Game("7 Wonders", 3, 7, 30, false, 2.3)
    );
  }

}
