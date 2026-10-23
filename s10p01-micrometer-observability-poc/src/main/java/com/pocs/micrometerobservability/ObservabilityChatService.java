package com.pocs.micrometerobservability;

import org.springframework.ai.chat.client.ChatClient;

/** Thin, provider-agnostic wrapper around a {@code ChatClient} call. */
public class ObservabilityChatService {

    private final ChatClient chatClient;

    public ObservabilityChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String ask(String question) {
        return chatClient.prompt().user(question).call().content();
    }
}
