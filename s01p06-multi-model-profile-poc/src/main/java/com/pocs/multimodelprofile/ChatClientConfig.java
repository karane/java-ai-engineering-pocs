package com.pocs.multimodelprofile;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean(name = "chatClient")
    @ConditionalOnProperty(prefix = "app", name = "provider", havingValue = "claude")
    public ChatClient claudeChatClient(AnthropicChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    @Bean(name = "chatClient")
    @ConditionalOnProperty(prefix = "app", name = "provider", havingValue = "gemini")
    public ChatClient geminiChatClient(GoogleGenAiChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}
