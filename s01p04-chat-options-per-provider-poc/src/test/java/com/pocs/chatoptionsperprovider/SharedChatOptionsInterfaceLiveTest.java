package com.pocs.chatoptionsperprovider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SharedChatOptionsInterfaceLiveTest {

    @Autowired
    private PitchService pitchService;

    @Autowired
    @Qualifier("anthropicChatClient")
    private ChatClient anthropicChatClient;

    @Autowired
    @Qualifier("geminiChatClient")
    private ChatClient geminiChatClient;

    @Test
    @EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
    void askWithSharedOptionsAcceptsAnthropicChatOptionsAgainstTheAnthropicClient() {
        AnthropicChatOptions options = AnthropicChatOptions.builder().temperature(0.4).maxTokens(64).build();

        String answer = pitchService.askWithSharedOptions(anthropicChatClient, "Reply with exactly one word: pong", options);

        assertThat(answer).isNotBlank();
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    void askWithSharedOptionsAcceptsGoogleGenAiChatOptionsAgainstTheGeminiClient() {
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder().temperature(0.4).maxOutputTokens(64).build();

        String answer = pitchService.askWithSharedOptions(geminiChatClient, "Reply with exactly one word: pong", options);

        assertThat(answer).isNotBlank();
    }
}
