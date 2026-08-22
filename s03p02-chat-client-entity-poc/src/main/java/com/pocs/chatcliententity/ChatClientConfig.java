package com.pocs.chatcliententity;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires a single ChatClient bean from the concrete AnthropicChatModel, same
 * shape as every other POC in this roadmap - autowiring the generic ChatModel
 * would be ambiguous once multiple provider starters are on the classpath.
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(AnthropicChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}
