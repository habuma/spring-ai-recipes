package com.example.autodreaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agent.dream.AutoDreamService;
import org.springaicommunity.agent.dream.DreamMode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DreamRunner {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(DreamRunner.class);

  private final AutoDreamService autoDreamService;

  public DreamRunner(ChatModel chatModel) {
    var chatClientBuilder = ChatClient.builder(chatModel);
    this.autoDreamService = AutoDreamService.builder(chatClientBuilder)
        .dreamMode(DreamMode.PROPOSE)
        .build();
  }

  @Async
  public void dream(String rootMemoriesDirectory) {
    try {
      var dreamResult = autoDreamService.runDreamCycle(rootMemoriesDirectory);
      LOGGER.info("Dream result status: {}", dreamResult.status());
      LOGGER.info("Dream result summary: {}", dreamResult.summary());
    }
    catch (Exception e) {
      LOGGER.error("Dream cycle failed", e);
    }
  }

}
