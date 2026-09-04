package com.example.chatloop;

import com.integrallis.models.api.SamplingOptions;
import com.integrallis.models.spring.ai.ModelsSpringAiChatModel;
import org.modeljars.ModelBackend;
import org.modeljars.ModelJars;
import org.modeljars.ModelLoadOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.modeljars.catalog.Qwen3_1_7b_Q8_0.MODEL;

@Configuration
public class ChatClientConfig {

  @Bean
  ChatClient.Builder chatClientBuilder(ObjectProvider<ChatClientBuilderCustomizer> customizers) {
    var defaults = SamplingOptions.builder().temperature(0).maxTokens(128).build();
    var loadOptions = ModelLoadOptions.builder().backend(ModelBackend.JAVA).build();

    var runtime = ModelJars.openRuntime(MODEL, loadOptions);
    var model = new ModelsSpringAiChatModel(
        runtime.model(),
        runtime.descriptor().alias(),
        runtime.chatTemplate(),
        defaults,
        runtime.descriptor().capabilities());

    return applyCustomizers(
        ChatClient.builder(model),
        customizers
    );
  }

  @Bean
  ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
    return chatClientBuilder.build();
  }

  @Bean
  ChatClientBuilderCustomizer chatMemoryCustomizer() {
    return builder -> {
      builder.defaultAdvisors(
          MessageChatMemoryAdvisor.builder(
                  MessageWindowChatMemory.builder()
                      .maxMessages(500)
                      .build())
              .build());
    };
  }

  @Bean
  ChatClientBuilderCustomizer addTools(WeatherTools weatherTools) {
    return builder -> builder.defaultTools(weatherTools);
  }

  private ChatClient.Builder applyCustomizers(
      ChatClient.Builder builder,
      ObjectProvider<ChatClientBuilderCustomizer> customizers) {

    customizers.orderedStream()
        .forEach(customizer -> customizer.customize(builder));
    return builder;
  }

}
