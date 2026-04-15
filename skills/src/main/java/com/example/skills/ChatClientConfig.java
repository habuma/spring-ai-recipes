package com.example.skills;

import org.springaicommunity.agent.tools.FileSystemTools;
import org.springaicommunity.agent.tools.ShellTools;
import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
public class ChatClientConfig {

  @Value("${agent.skills.path}")
  List<Resource> skillResources;

  @Bean
  ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
    return chatClientBuilder.build();
  }

  @Bean
  ChatClientCustomizer addSkills() {
    return builder -> builder
        .defaultSystem("IMPORTANT: Always use the available skills to " +
                "assist the user in their requests. When available follow " +
                "skills instructions exactly.")
        .defaultToolCallbacks(SkillsTool.builder()
                .addSkillsResources(skillResources).build());
  }

  @Bean
  ChatClientCustomizer addTools(WeatherTools weatherTools) {
    return builder -> builder
        .defaultTools(weatherTools);
  }

}
