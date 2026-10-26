package com.pocs.tokenusagecosttracking;

/**
 * Snapshot of one chat call's token usage and resulting cost - what {@link TokenUsageService#ask} hands
 * back alongside the model's text answer, so a caller (the demo runner, a live test) can inspect usage
 * without re-parsing a {@code ChatResponse} itself.
 */
public record TokenUsageResult(
        Provider provider,
        String answer,
        int promptTokens,
        int completionTokens,
        int totalTokens,
        double costUsd) {
}
