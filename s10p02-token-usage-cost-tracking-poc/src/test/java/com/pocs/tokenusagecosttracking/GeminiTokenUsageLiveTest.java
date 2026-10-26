package com.pocs.tokenusagecosttracking;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Same proof as {@link AnthropicTokenUsageLiveTest}, against the real Gemini API.
 */
@SpringBootTest
class GeminiTokenUsageLiveTest {

    @Autowired
    private TokenUsageService tokenUsageService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    void realCallReportsUsageAndCostAndUpdatesTheGauge() {
        TokenUsageResult result = tokenUsageService.ask(Provider.GEMINI,
                "Reply with exactly one short sentence about databases.");

        assertThat(result.answer()).isNotBlank();
        assertThat(result.promptTokens()).isGreaterThan(0);
        assertThat(result.completionTokens()).isGreaterThan(0);
        assertThat(result.costUsd()).isGreaterThan(0.0);

        Gauge gauge = meterRegistry.find("gen_ai.client.cost.usd").tag("provider", "gemini").gauge();
        assertThat(gauge).as("expected a gen_ai.client.cost.usd gauge tagged provider=gemini").isNotNull();
        assertThat(gauge.value()).isEqualTo(result.costUsd());
    }
}
