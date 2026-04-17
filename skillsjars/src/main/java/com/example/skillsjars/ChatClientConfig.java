package com.example.skillsjars;

import org.springaicommunity.agent.tools.FileSystemTools;
import org.springaicommunity.agent.tools.ShellTools;
import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
public class ChatClientConfig {

  @Bean
  ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
    return chatClientBuilder.build();
  }

  @Bean
  ChatClientCustomizer chatMemoryCustomizer() {
    return builder -> {
      builder.defaultAdvisors(
          MessageChatMemoryAdvisor.builder(
                  MessageWindowChatMemory.builder()
                      .maxMessages(500)
                      .build())
              .build());
    };
  }

  @Value("${agent.skills.paths}")
  List<Resource> skillPaths;

  @Bean
  ChatClientCustomizer skillsTools() {
    return builder -> builder
        .defaultToolCallbacks(
            SkillsTool.builder()
                .addSkillsResources(skillPaths)
                .build());
  }

  @Bean
  ChatClientCustomizer addFileSystemAndShellTools() {
    return builder -> builder
        .defaultTools(
            ShellTools.builder().build(),
            FileSystemTools.builder().build());
  }

}
