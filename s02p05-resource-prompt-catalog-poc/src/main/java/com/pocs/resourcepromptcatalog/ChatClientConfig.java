package com.pocs.resourcepromptcatalog;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Single-provider wiring (Claude), same shape as s01p01: the autoconfigured
 * AnthropicChatModel is wrapped once into a ChatClient bean. The point of this
 * POC is the PromptTemplateRegistry abstraction, which is provider-agnostic -
 * it just hands rendered prompt strings to whichever ChatClient is wired here.
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(AnthropicChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}
