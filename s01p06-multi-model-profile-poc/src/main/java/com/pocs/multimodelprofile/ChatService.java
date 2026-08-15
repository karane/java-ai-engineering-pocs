package com.pocs.multimodelprofile;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String ask(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    public Flux<String> streamAsk(String question) {
        return chatClient.prompt()
                .user(question)
                .stream()
                .content();
    }
}
