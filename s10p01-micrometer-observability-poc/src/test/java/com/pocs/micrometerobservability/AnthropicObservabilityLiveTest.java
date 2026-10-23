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

/** Proves the observability wiring end to end against the real Anthropic API. */
@SpringBootTest
class AnthropicObservabilityLiveTest {

    @Autowired
    @Qualifier("anthropicObservedChatClient")
    private ChatClient anthropicObservedChatClient;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    @EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
    void realCallIsRecordedWithThePocCustomTag() {
        String answer = new ObservabilityChatService(anthropicObservedChatClient)
                .ask("Reply with exactly one short sentence about databases.");
        assertThat(answer).isNotBlank();

        Timer timer = meterRegistry.find("gen_ai.client.operation")
                .tag("poc.module", "s10p01")
                .tag("gen_ai.system", "anthropic")
                .timer();
        assertThat(timer).as("expected a gen_ai.client.operation timer tagged poc.module=s10p01").isNotNull();
        assertThat(timer.count()).isGreaterThanOrEqualTo(1);
    }
}
