package com.pocs.promptstuffingcontext;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plain unit tests, no Spring context and no API key needed: they only exercise
 * ContextStuffingService's token estimation and truncation logic directly, using
 * the real JTokkitTokenCountEstimator (a pure local computation, no network call)
 * - proving the budgeting/truncation behavior works before any live-call test runs.
 */
class ContextStuffingServiceTests {

    private static final int MAX_PROMPT_TOKENS = 60;

    private final ContextStuffingService service =
            new ContextStuffingService(new JTokkitTokenCountEstimator(), MAX_PROMPT_TOKENS);

    @Test
    void estimateTokensReturnsAPositiveCountForNonEmptyText() {
        int tokens = service.estimateTokens("The quick brown fox jumps over the lazy dog.");

        assertThat(tokens).isPositive();
    }

    @Test
    void estimateTokensGrowsWithLongerText() {
        int shortCount = service.estimateTokens("Hello world.");
        int longCount = service.estimateTokens("Hello world. ".repeat(50));

        assertThat(longCount).isGreaterThan(shortCount);
    }

    @Test
    void shortContextFitsWithinBudgetAndIsNotTruncated() {
        String context = "Spring AI renders PromptTemplate placeholders using st4 syntax.";

        ContextStuffingService.StuffedPrompt result = service.buildPrompt(context, "What syntax is used?");

        assertThat(result.truncated()).isFalse();
        assertThat(result.contextUsed()).isEqualTo(context);
        assertThat(result.finalTokenCount()).isEqualTo(result.originalTokenCount());
        assertThat(result.finalTokenCount()).isLessThanOrEqualTo(MAX_PROMPT_TOKENS);
        assertThat(result.renderedPrompt()).contains(context).contains("What syntax is used?");
    }

    @Test
    void longContextIsTruncatedToFitTheBudget() {
        String longContext = "Retrieval-augmented generation combines a retrieval step with a "
                .repeat(20);

        ContextStuffingService.StuffedPrompt result = service.buildPrompt(longContext, "Why truncate?");

        assertThat(result.truncated()).isTrue();
        assertThat(result.contextUsed().length()).isLessThan(longContext.length());
        assertThat(result.originalTokenCount()).isGreaterThan(MAX_PROMPT_TOKENS);
        assertThat(result.finalTokenCount()).isLessThanOrEqualTo(MAX_PROMPT_TOKENS);
    }

    @Test
    void truncatedPromptStillContainsTheFullQuestion() {
        String longContext = "Context filler sentence describing retrieved information. ".repeat(30);
        String question = "What is the key takeaway?";

        ContextStuffingService.StuffedPrompt result = service.buildPrompt(longContext, question);

        assertThat(result.renderedPrompt()).contains(question);
    }

    @Test
    void emptyContextNeverThrowsEvenUnderAVeryTightBudget() {
        ContextStuffingService tightService = new ContextStuffingService(new JTokkitTokenCountEstimator(), 5);

        ContextStuffingService.StuffedPrompt result = tightService.buildPrompt(
                "Some retrieved context that is definitely too long for five tokens.", "Question?");

        assertThat(result.renderedPrompt()).isNotNull();
    }
}
