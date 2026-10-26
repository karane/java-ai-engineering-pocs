package com.pocs.tokenusagecosttracking;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.Usage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Unit tests for {@link CostCalculator} - no Spring context, no network, no API key. Hand-builds a
 * {@link DefaultUsage} (the same implementation Spring AI's own {@code ChatModel}s populate on a real
 * {@code ChatResponse}) and asserts the arithmetic against the pricing constants in {@link Provider}.
 */
class CostCalculatorTests {

    @Test
    void computesCostFromPromptAndCompletionTokensAtAnthropicPricing() {
        Usage usage = new DefaultUsage(1000, 1000);

        double costUsd = CostCalculator.costUsd(Provider.ANTHROPIC, usage);

        // 1000 prompt tokens @ $0.0010/1K + 1000 completion tokens @ $0.0050/1K = 0.0010 + 0.0050
        assertThat(costUsd).isCloseTo(0.0060, within(1e-9));
    }

    @Test
    void computesCostFromPromptAndCompletionTokensAtGeminiPricing() {
        Usage usage = new DefaultUsage(1000, 1000);

        double costUsd = CostCalculator.costUsd(Provider.GEMINI, usage);

        // 1000 prompt tokens @ $0.00010/1K + 1000 completion tokens @ $0.00040/1K = 0.00010 + 0.00040
        assertThat(costUsd).isCloseTo(0.00050, within(1e-9));
    }

    @Test
    void zeroTokensMeansZeroCost() {
        Usage usage = new DefaultUsage(0, 0);

        assertThat(CostCalculator.costUsd(Provider.ANTHROPIC, usage)).isZero();
        assertThat(CostCalculator.costUsd(Provider.GEMINI, usage)).isZero();
    }

    @Test
    void nullTokenCountsAreTreatedAsZero() {
        Usage usage = new DefaultUsage(null, null);

        assertThat(CostCalculator.costUsd(Provider.ANTHROPIC, usage)).isZero();
    }

    @Test
    void claudeIsMoreExpensiveThanGeminiForTheSameTokenCounts() {
        Usage usage = new DefaultUsage(500, 500);

        double claudeCost = CostCalculator.costUsd(Provider.ANTHROPIC, usage);
        double geminiCost = CostCalculator.costUsd(Provider.GEMINI, usage);

        assertThat(claudeCost).isGreaterThan(geminiCost);
    }
}
