package com.example.chatloop;

record Game(
    String name,
    int minPlayers,
    int maxPlayers,
    int playTimeMinutes,
    boolean cooperative,
    double complexity) {}
