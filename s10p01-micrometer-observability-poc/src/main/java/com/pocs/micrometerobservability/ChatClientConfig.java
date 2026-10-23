package com.pocs.micrometerobservability;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires one observed {@code ChatClient} per provider, using the custom observation convention. */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient anthropicObservedChatClient(AnthropicChatModel chatModel) {
        chatModel.setObservationConvention(new PocChatModelObservationConvention());
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public ChatClient geminiObservedChatClient(GoogleGenAiChatModel chatModel) {
        chatModel.setObservationConvention(new PocChatModelObservationConvention());
        return ChatClient.builder(chatModel).build();
    }
}
