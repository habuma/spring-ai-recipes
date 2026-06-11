package com.example.skills;

import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
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
  ChatClientBuilderCustomizer addSkills() {
    return builder -> builder
        .defaultSystem("IMPORTANT: Always use the available skills to " +
                "assist the user in their requests. When available follow " +
                "skills instructions exactly.")
        .defaultTools(SkillsTool.builder()
                .addSkillsResources(skillResources).build());
  }

  @Bean
  ChatClientBuilderCustomizer addTools(WeatherTools weatherTools) {
    return builder -> builder
        .defaultTools(weatherTools);
  }

}
