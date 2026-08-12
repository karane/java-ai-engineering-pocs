package com.pocs.chatclientfluent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClientCustomizer defaultPersonaCustomizer() {
        return builder -> builder.defaultSystem(
                "You are a terse Spring AI expert. Answer in at most two sentences.");
    }

    @Bean
    public ChatClient defaultChatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }

    @Bean
    public ChatClient manualChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
