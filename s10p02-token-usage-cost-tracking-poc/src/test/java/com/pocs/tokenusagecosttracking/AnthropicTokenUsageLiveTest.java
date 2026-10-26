package com.pocs.tokenusagecosttracking;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the full chain against the real Anthropic API: a real call returns non-zero prompt/completion
 * token counts, {@link CostCalculator}'s arithmetic on those real counts yields a positive cost, and that
 * cost shows up in the {@code gen_ai.client.cost.usd} gauge tagged {@code provider=anthropic}.
 */
@SpringBootTest
class AnthropicTokenUsageLiveTest {

    @Autowired
    private TokenUsageService tokenUsageService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    @EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
    void realCallReportsUsageAndCostAndUpdatesTheGauge() {
        TokenUsageResult result = tokenUsageService.ask(Provider.ANTHROPIC,
                "Reply with exactly one short sentence about databases.");

        assertThat(result.answer()).isNotBlank();
        assertThat(result.promptTokens()).isGreaterThan(0);
        assertThat(result.completionTokens()).isGreaterThan(0);
        assertThat(result.totalTokens()).isEqualTo(result.promptTokens() + result.completionTokens());
        assertThat(result.costUsd()).isGreaterThan(0.0);

        Gauge gauge = meterRegistry.find("gen_ai.client.cost.usd").tag("provider", "anthropic").gauge();
        assertThat(gauge).as("expected a gen_ai.client.cost.usd gauge tagged provider=anthropic").isNotNull();
        assertThat(gauge.value()).isEqualTo(result.costUsd());
    }
}
