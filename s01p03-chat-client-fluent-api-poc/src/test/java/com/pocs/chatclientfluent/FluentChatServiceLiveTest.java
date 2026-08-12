package com.pocs.chatclientfluent;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class FluentChatServiceLiveTest {

    private static final String QUESTION = "Reply with exactly one word: pong";

    @Autowired
    private FluentChatService fluentChatService;

    @Test
    void defaultPersonaProducesNonEmptyContent() {
        String answer = fluentChatService.askWithDefaultPersona(QUESTION);
        assertThat(answer).isNotBlank();
    }

    @Test
    void systemPromptOverrideChangesBehaviorButStillAnswers() {
        String answer = fluentChatService.askWithSystemOverride(
                "You always answer in French.", "How do you say hello?");
        assertThat(answer).isNotBlank();
    }

    @Test
    void manualChatClientWithoutPersonaStillWorks() {
        String answer = fluentChatService.askWithNoPersona(QUESTION);
        assertThat(answer).isNotBlank();
    }

    @Test
    void streamingContentEmitsAtLeastOneChunk() {
        StepVerifier.create(fluentChatService.streamAnswer(QUESTION))
                .expectNextCount(1)
                .thenConsumeWhile(chunk -> true)
                .expectComplete()
                .verify(Duration.ofSeconds(30));
    }

    @Test
    void streamingChatResponseEmitsAtLeastOneChunk() {
        StepVerifier.create(fluentChatService.streamChatResponses(QUESTION))
                .expectNextCount(1)
                .thenConsumeWhile(chunk -> true)
                .expectComplete()
                .verify(Duration.ofSeconds(30));
    }

    @Test
    void fullResponseExposesFinishReasonAndTokenUsage() {
        ChatResponse response = fluentChatService.askForFullResponse(QUESTION);

        assertThat(response.getResult().getOutput().getText()).isNotBlank();
        assertThat(response.getResult().getMetadata().getFinishReason()).isNotBlank();
        assertThat(response.getMetadata().getUsage().getTotalTokens()).isGreaterThan(0);

        String description = fluentChatService.describeGeneration(response);
        assertThat(description).contains("finishReason=").contains("totalTokens=");
    }
}
