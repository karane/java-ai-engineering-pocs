package com.pocs.chatoptionsperprovider;

import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PitchService {

    private final ChatClient anthropicChatClient;
    private final ChatClient anthropicTopPChatClient;
    private final ChatClient geminiChatClient;

    public PitchService(@Qualifier("anthropicChatClient") ChatClient anthropicChatClient,
                         @Qualifier("anthropicTopPChatClient") ChatClient anthropicTopPChatClient,
                         @Qualifier("geminiChatClient") ChatClient geminiChatClient) {
        this.anthropicChatClient = anthropicChatClient;
        this.anthropicTopPChatClient = anthropicTopPChatClient;
        this.geminiChatClient = geminiChatClient;
    }

    public String anthropicPitchWithDefaults(String product) {
        return anthropicChatClient.prompt()
                .user(promptFor(product))
                .call()
                .content();
    }

    public String anthropicPitchWithOptions(String product, PitchTuning tuning) {
        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .temperature(tuning.creativity())
                .maxTokens(tuning.maxTokens())
                .build();
        return askWithSharedOptions(anthropicChatClient, promptFor(product), options);
    }

    public String anthropicPitchWithTopP(String product, PitchTuning tuning) {
        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .topP(tuning.topP())
                .maxTokens(tuning.maxTokens())
                .build();
        return askWithSharedOptions(anthropicTopPChatClient, promptFor(product), options);
    }

    public String geminiPitchWithDefaults(String product) {
        return geminiChatClient.prompt()
                .user(promptFor(product))
                .call()
                .content();
    }

    public String geminiPitchWithOptions(String product, PitchTuning tuning) {
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .temperature(tuning.creativity())
                .topK(tuning.topK())
                .candidateCount(1)
                .maxOutputTokens(tuning.maxTokens())
                .build();
        return askWithSharedOptions(geminiChatClient, promptFor(product), options);
    }

    public String askWithSharedOptions(ChatClient client, String question, ChatOptions options) {
        return client.prompt()
                .user(question)
                .options(options)
                .call()
                .content();
    }

    private String promptFor(String product) {
        return "Write a one-sentence marketing tagline for: " + product;
    }
}
