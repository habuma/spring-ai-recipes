package com.example.reactagentfun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DisneyParksTools {

  private static final Logger LOGGER = LoggerFactory.getLogger(DisneyParksTools.class);

  private static final Map<String, AttractionInfo> ATTRACTION_INFOS = new HashMap<>();

  static {
    ATTRACTION_INFOS.put("Big Thunder Mountain Railroad",
        new AttractionInfo("Big Thunder Mountain Railroad", "Frontierland", 40, 45));
    ATTRACTION_INFOS.put("Space Mountain",
        new AttractionInfo("Space Mountain", "Tomorrowland", 40, 55));
    ATTRACTION_INFOS.put("Star Tours",
        new AttractionInfo("Star Tours", "Tomorrowland", 40, 10));
    ATTRACTION_INFOS.put("Matterhorn Bobsleds",
        new AttractionInfo("Matterhorn Bobsleds", "Fantasyland", 42, 55));
    ATTRACTION_INFOS.put("Haunted Mansion",
        new AttractionInfo("Haunted Mansion", "New Orleans Square", null, 25));
    ATTRACTION_INFOS.put("Pirates of the Caribbean",
        new AttractionInfo("Pirates of the Caribbean", "New Orleans Square", null, 30));
  }


  @Tool(name = "get-attraction-info",
      description = """
        Gets information for an attraction, including its current wait time,
        land, and height requirement.
        """)
  public AttractionInfo getAttractionInfo(
      @ToolParam(description = "The name of the attraction")
      String attractionName) {
    LOGGER.info("GETTING ATTRACTION INFO: " + attractionName);
    return ATTRACTION_INFOS.get(attractionName);
  }

  @Tool(name = "pick-next-ride",
      description = """
        Picks the next attraction to ride from the supplied attraction information.
        Prefers the shortest wait time, but adds 5 minutes when the attraction
        is in a different land from the user's current land.
        """)
  public AttractionInfo pickNextRide(
      @ToolParam(description = "The land the user is currently in")
      String currentLand,

      @ToolParam(description = "Information about the attractions being considered")
      List<AttractionInfo> attractions) {
    LOGGER.info("PICK NEXT RIDE: " + currentLand);
    return attractions.stream()
        .min(Comparator.comparingInt(attraction ->
            attraction.currentWaitTime()
                + (attraction.land().equalsIgnoreCase(currentLand) ? 0 : 5)))
        .orElseThrow();
  }

}
