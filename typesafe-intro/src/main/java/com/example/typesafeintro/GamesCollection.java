package com.example.typesafeintro;

public class GamesCollection {

  public static final String UNO = """
      Game: Uno
      Players: 2-10
      Playing time: 15-30 minutes
      
      Players take turns playing cards that match the color, number, or symbol
      of the card currently on top of the discard pile. Special action cards can
      skip another player's turn, reverse the direction of play, force another
      player to draw cards, or change the current color. The goal is to be the
      first player to get rid of all of their cards. The game has only a few
      basic rules, requires little advance planning, and can be taught to new
      players in just a few minutes.
      """;

  public static final String TICKET_TO_RIDE = """
      Game: Ticket to Ride
      Players: 2-5
      Playing time: 30-60 minutes
      
      Players collect sets of colored train cards and use them to claim railway
      routes connecting cities across a map. Players score points by completing
      destination tickets and building routes, while competing with other players
      for limited connections. Turns consist primarily of drawing cards, claiming
      a route, or drawing new destination tickets. The basic rules can be explained
      in a few minutes, although players must make strategic decisions about which
      routes to pursue and when to claim them.
      """;

  public static final String FORBIDDEN_ISLAND = """
      Game: Forbidden Island
      Players: 2-4
      Playing time: 30 minutes
      
      Players work together as a team of adventurers trying to collect four
      treasures and escape from a sinking island. Each player has a unique role
      with a special ability that can help the group. On their turns, players
      move around the island, shore up flooded locations, exchange treasure
      cards, and collect treasures. After each turn, additional areas of the
      island may flood or sink permanently. All players win together if they
      recover the treasures and escape, or lose together if the island sinks
      before they can do so. The basic actions and objectives are straightforward,
      although players must coordinate their actions and manage increasing risks.
      """;

  public static final String ONE_NIGHT_ULTIMATE_WEREWOLF = """
      Game: One Night Ultimate Werewolf
      Players: 3-10
      Playing time: 10 minutes
      
      Each player receives a secret role belonging to either the village team or
      the werewolf team. During a brief night phase, players perform actions based
      on their roles. They then discuss what happened, make claims about their
      identities, attempt to determine who is telling the truth, and ultimately
      vote for another player. There is no player elimination and each complete
      game lasts only a few minutes, encouraging groups to play several times.
      """;

  public static final String CASCADIA = """
      Game: Cascadia
      Players: 1-4
      Playing time: 30-45 minutes
      
      Players take turns selecting combinations of habitat tiles and wildlife
      tokens to build their own individual ecosystems. Points are awarded for
      creating large connected habitats and arranging animals according to
      different scoring patterns. Each player builds a separate environment,
      with interaction occurring primarily through competition for the available
      tile and wildlife choices. The game has relatively few rules, but players
      must balance several spatial and scoring objectives.
      """;

  public static final String WINGSPAN = """
      Game: Wingspan
      Players: 1-5
      Playing time: 40-70 minutes
      
      Players compete to build wildlife preserves by attracting birds to forest,
      grassland, and wetland habitats. Bird cards provide different abilities that
      can combine to create increasingly effective actions as the game progresses.
      Players manage food, eggs, cards, and habitat actions while pursuing shared
      and private scoring goals. Players primarily develop their own preserves,
      although they compete for resources and goals and some bird abilities can
      affect other players.
      """;

  public static final String SEVEN_WONDERS = """
      Game: 7 Wonders
      Players: 3-7
      Playing time: 30 minutes
      
      Each player develops an ancient civilization over three ages by drafting
      cards representing resources, commerce, military strength, science, and
      civic structures. On each turn, players simultaneously choose a card from
      their hand and pass the remaining cards to a neighboring player. Players
      must consider their neighbors' civilizations when making military, resource,
      and drafting decisions. Multiple scoring systems operate simultaneously,
      and new players must learn the purposes of several card types and understand
      how resources and symbols interact.
      """;

  public static final Game[] GAMES = {
      new Game("Uno", UNO),
      new Game("Ticket To Ride", TICKET_TO_RIDE),
      new Game("Forbidden Island", FORBIDDEN_ISLAND),
      new Game("One Night Ultimate Werewolf",  ONE_NIGHT_ULTIMATE_WEREWOLF),
      new Game("Cascadia", CASCADIA),
      new Game("Wingspan", WINGSPAN),
      new Game("7 Wonders", SEVEN_WONDERS),
  };
}
