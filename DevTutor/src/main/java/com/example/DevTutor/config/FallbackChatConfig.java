package com.example.DevTutor.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class FallbackChatConfig {

    @Bean
    @Primary
    public ChatModel fallbackChatModel(
            GoogleGenAiChatModel gemini,
            @Value("${app.ai.fallback-model}") String fallbackModel) {

        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                try {
                    return gemini.call(prompt);
                } catch (RuntimeException e) {
                    ChatOptions options = prompt.getOptions() != null
                            ? prompt.getOptions()
                            : gemini.getOptions();
                    return gemini.call(prompt.mutate()
                            .chatOptions(options.mutate().model(fallbackModel).build())
                            .build());
                }
            }

            @Override
            public ChatOptions getOptions() {
                return gemini.getOptions();
            }
        };
    }
}
