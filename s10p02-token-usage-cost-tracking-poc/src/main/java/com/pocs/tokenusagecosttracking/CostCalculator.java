package com.pocs.tokenusagecosttracking;

import org.springframework.ai.chat.metadata.Usage;

/** Turns a {@link Usage}'s token counts into a dollar figure using {@link Provider}'s pricing. */
public final class CostCalculator {

    private CostCalculator() {
    }

    public static double costUsd(Provider provider, Usage usage) {
        int promptTokens = usage.getPromptTokens() == null ? 0 : usage.getPromptTokens();
        int completionTokens = usage.getCompletionTokens() == null ? 0 : usage.getCompletionTokens();

        double promptCost = (promptTokens / 1000.0) * provider.promptPricePerThousandTokensUsd();
        double completionCost = (completionTokens / 1000.0) * provider.completionPricePerThousandTokensUsd();
        return promptCost + completionCost;
    }
}
