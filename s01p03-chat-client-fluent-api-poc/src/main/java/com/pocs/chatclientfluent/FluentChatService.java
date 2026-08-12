package com.pocs.chatclientfluent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class FluentChatService {

    private final ChatClient defaultChatClient;
    private final ChatClient manualChatClient;

    public FluentChatService(@Qualifier("defaultChatClient") ChatClient defaultChatClient,
                              @Qualifier("manualChatClient") ChatClient manualChatClient) {
        this.defaultChatClient = defaultChatClient;
        this.manualChatClient = manualChatClient;
    }

    public String askWithDefaultPersona(String question) {
        return defaultChatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    public String askWithSystemOverride(String system, String question) {
        return defaultChatClient.prompt()
                .system(system)
                .user(question)
                .call()
                .content();
    }

    public String askWithNoPersona(String question) {
        return manualChatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    public Flux<String> streamAnswer(String question) {
        return defaultChatClient.prompt()
                .user(question)
                .stream()
                .content();
    }

    public Flux<ChatResponse> streamChatResponses(String question) {
        return defaultChatClient.prompt()
                .user(question)
                .stream()
                .chatResponse();
    }

    public ChatResponse askForFullResponse(String question) {
        return defaultChatClient.prompt()
                .user(question)
                .call()
                .chatResponse();
    }

    public String describeGeneration(ChatResponse response) {
        Generation generation = response.getResult();
        String finishReason = generation.getMetadata().getFinishReason();
        Integer totalTokens = response.getMetadata().getUsage().getTotalTokens();
        return "finishReason=%s, totalTokens=%d, text=%s".formatted(
                finishReason, totalTokens, generation.getOutput().getText());
    }
}
