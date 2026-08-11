package com.pocs.springaigemini;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class GeminiServiceLiveTest {

    @Autowired
    private GeminiService geminiService;

    @Test
    void callReturnsNonEmptyContentFromGemini() {
        String answer = geminiService.ask("Reply with exactly one word: pong");
        assertThat(answer).isNotBlank();
    }

    @Test
    void callWithMetadataExposesTokenUsage() {
        ChatResponse response = geminiService.askWithMetadata("Reply with exactly one word: pong");
        assertThat(response.getResult().getOutput().getText()).isNotBlank();
        assertThat(response.getMetadata().getUsage().getTotalTokens()).isGreaterThan(0);
    }
}
