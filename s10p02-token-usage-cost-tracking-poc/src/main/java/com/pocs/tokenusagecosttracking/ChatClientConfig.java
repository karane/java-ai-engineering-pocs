package com.pocs.tokenusagecosttracking;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Same dual-provider wiring pattern as {@code s06p03}'s {@code ChatClientConfig}: both {@code ChatClient}
 * beans always exist, one per concrete {@code ChatModel}, since this POC's whole point is comparing the
 * two providers' token usage and cost side by side.
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient anthropicChatClient(AnthropicChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public ChatClient geminiChatClient(GoogleGenAiChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
