package com.pocs.springaigemini;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private final ChatClient chatClient;

    public GeminiService(ChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
    }

    public String ask(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    public ChatResponse askWithMetadata(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .chatResponse();
    }
}
