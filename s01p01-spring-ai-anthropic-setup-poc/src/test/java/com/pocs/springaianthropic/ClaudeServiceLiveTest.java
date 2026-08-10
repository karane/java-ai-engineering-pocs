package com.pocs.springaianthropic;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class ClaudeServiceLiveTest {

    @Autowired
    private ClaudeService claudeService;

    @Test
    void callReturnsNonEmptyContentFromClaude() {
        String answer = claudeService.ask("Reply with exactly one word: pong");
        assertThat(answer).isNotBlank();
    }

    @Test
    void callWithMetadataExposesTokenUsage() {
        ChatResponse response = claudeService.askWithMetadata("Reply with exactly one word: pong");
        assertThat(response.getResult().getOutput().getText()).isNotBlank();
        assertThat(response.getMetadata().getUsage().getTotalTokens()).isGreaterThan(0);
    }
}
