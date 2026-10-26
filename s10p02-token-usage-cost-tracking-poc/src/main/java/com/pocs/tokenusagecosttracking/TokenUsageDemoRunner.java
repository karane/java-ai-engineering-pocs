package com.pocs.tokenusagecosttracking;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Asks the same question of both providers (whichever has a key set), printing token usage and
 * cost-per-request for each - a quick side-by-side "which provider is cheaper for this prompt" view
 * without needing the JUnit harness.
 */
@Component
public class TokenUsageDemoRunner implements CommandLineRunner {

    private static final String DEMO_QUESTION =
            "In two or three sentences, explain what a vector database is used for.";

    private final TokenUsageService tokenUsageService;

    public TokenUsageDemoRunner(TokenUsageService tokenUsageService) {
        this.tokenUsageService = tokenUsageService;
    }

    @Override
    public void run(String... args) {
        System.out.println("[s10p02] Question: " + DEMO_QUESTION);

        if (isSet("ANTHROPIC_API_KEY")) {
            printUsage(tokenUsageService.ask(Provider.ANTHROPIC, DEMO_QUESTION));
        } else {
            System.out.println("[s10p02] --- Claude (Anthropic) --- skipped, ANTHROPIC_API_KEY not set");
        }

        if (isSet("GEMINI_API_KEY")) {
            printUsage(tokenUsageService.ask(Provider.GEMINI, DEMO_QUESTION));
        } else {
            System.out.println("[s10p02] --- Gemini --- skipped, GEMINI_API_KEY not set");
        }
    }

    private void printUsage(TokenUsageResult result) {
        System.out.printf("[s10p02] --- %s (%s) ---%n", result.provider().tag(), result.provider().model());
        System.out.println("[s10p02] " + result.answer());
        System.out.printf("[s10p02] promptTokens=%d completionTokens=%d totalTokens=%d costUsd=%.6f%n",
                result.promptTokens(), result.completionTokens(), result.totalTokens(), result.costUsd());
    }

    private boolean isSet(String envVar) {
        String value = System.getenv(envVar);
        return value != null && !value.isBlank();
    }
}
