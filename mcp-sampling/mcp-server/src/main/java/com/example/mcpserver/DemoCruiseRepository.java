package com.example.mcpserver;

import org.springframework.stereotype.Service;

@Service
public class DemoCruiseRepository implements CruiseRepository {

  @Override
  public CruiseDetails getCruiseDetails(String cruiseId) {
    return new CruiseDetails(
        "Disney Magic",
        new Day[] {
            new Day("2026-05-16",
                "Departure from Vancouver, BC"),
            new Day("2026-05-17",
                "Day at Sea"),
            new Day("2026-05-18",
                "Glacier Viewing (Stikine Icecap"),
            new Day("2026-05-19",
                "Ketchikan, AK"),
            new Day("2026-05-20",
                "Day at Sea"),
            new Day("2026-05-21",
                "Disembark Vancouver, BC (8:00am)"),
        }
    );
  }
}
