package com.pocs.multimodelprofile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("claude")
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class ClaudeChatServiceLiveTest {

    @Autowired
    private ChatService chatService;

    @Test
    void askReturnsNonEmptyAnswerFromClaude() {
        String answer = chatService.ask("Reply with the single word: pong");
        assertThat(answer).isNotBlank();
    }

    @Test
    void streamAskAssemblesToANonEmptyAnswerFromClaude() {
        String answer = chatService.streamAsk("Reply with the single word: pong")
                .collect(StringBuilder::new, StringBuilder::append)
                .map(StringBuilder::toString)
                .block();
        assertThat(answer).isNotBlank();
    }
}
