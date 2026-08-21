package com.pocs.beanoutputconverter;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Single-provider wiring (Claude), same shape as s01p01/s02p05: the
 * autoconfigured AnthropicChatModel is wrapped once into a ChatClient bean.
 * Autowiring the generic ChatModel would be ambiguous once multiple provider
 * starters are on the classpath, so we depend on the concrete
 * AnthropicChatModel type here.
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(AnthropicChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}
