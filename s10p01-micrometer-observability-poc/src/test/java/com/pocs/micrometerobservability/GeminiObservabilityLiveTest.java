package com.pocs.micrometerobservability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Same proof as {@link AnthropicObservabilityLiveTest}, against the real Gemini API. */
@SpringBootTest
class GeminiObservabilityLiveTest {

    @Autowired
    @Qualifier("geminiObservedChatClient")
    private ChatClient geminiObservedChatClient;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    void realCallIsRecordedWithThePocCustomTag() {
        String answer = new ObservabilityChatService(geminiObservedChatClient)
                .ask("Reply with exactly one short sentence about databases.");
        assertThat(answer).isNotBlank();

        Timer timer = meterRegistry.find("gen_ai.client.operation")
                .tag("poc.module", "s10p01")
                .timer();
        assertThat(timer).as("expected a gen_ai.client.operation timer tagged poc.module=s10p01").isNotNull();
        assertThat(timer.count()).isGreaterThanOrEqualTo(1);
    }
}
