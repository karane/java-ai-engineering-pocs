package com.pocs.ragquality;

import org.springframework.ai.chat.client.ChatClient;

/**
 * Thin wrapper around a RAG-augmented {@link ChatClient} call. Kept provider-agnostic on purpose: the
 * same method runs against either the Anthropic-backed or the Gemini-backed {@code ChatClient} bean, and
 * against the stub {@code ChatModel} in tests - it never knows or cares which one it got.
 */
public class RagService {

    private final ChatClient chatClient;

    public RagService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String ask(String question) {
        return chatClient.prompt().user(question).call().content();
    }
}
