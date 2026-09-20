package com.pocs.contextualcompression;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBudgetTests {

    @Test
    void estimateTokensUsesCharsPerFourHeuristic() {
        assertThat(TokenBudget.estimateTokens("")).isZero();
        assertThat(TokenBudget.estimateTokens("abcd")).isEqualTo(1);
        assertThat(TokenBudget.estimateTokens("abcde")).isEqualTo(2);
        assertThat(TokenBudget.estimateTokens("a".repeat(400))).isEqualTo(100);
    }

    @Test
    void selectWithinBudgetKeepsAContiguousPrefixThatFits() {
        // Each chunk is ~100 chars -> ~25 tokens.
        List<Document> ranked = List.of(
                chunkOf("first", 100),
                chunkOf("second", 100),
                chunkOf("third", 100),
                chunkOf("fourth", 100));

        List<Document> selected = TokenBudget.selectWithinBudget(ranked, 60);

        assertThat(selected).hasSize(2);
        assertThat(selected).extracting(Document::getText).containsExactly(
                ranked.get(0).getText(), ranked.get(1).getText());
    }

    @Test
    void smallerBudgetIncludesFewerOrEqualChunksThanALargerBudgetForTheSameDocuments() {
        List<Document> ranked = List.of(
                chunkOf("a", 200),
                chunkOf("b", 200),
                chunkOf("c", 200),
                chunkOf("d", 200),
                chunkOf("e", 200));

        List<Document> claudeClassSelection = TokenBudget.selectWithinBudget(ranked,
                TokenBudget.CLAUDE_HAIKU_CONTEXT_BUDGET_TOKENS);
        List<Document> geminiClassSelection = TokenBudget.selectWithinBudget(ranked,
                TokenBudget.GEMINI_FLASH_CONTEXT_BUDGET_TOKENS);

        assertThat(TokenBudget.CLAUDE_HAIKU_CONTEXT_BUDGET_TOKENS)
                .isLessThan(TokenBudget.GEMINI_FLASH_CONTEXT_BUDGET_TOKENS);
        assertThat(claudeClassSelection.size()).isLessThanOrEqualTo(geminiClassSelection.size());
        // With this corpus, the smaller Claude-class budget must actually bind (not both budgets
        // trivially fitting everything), or the comparison above wouldn't prove anything.
        assertThat(claudeClassSelection.size()).isLessThan(ranked.size());
        assertThat(geminiClassSelection).hasSize(ranked.size());
    }

    private Document chunkOf(String label, int length) {
        return Document.builder().text(label.repeat(length / label.length() + 1).substring(0, length)).build();
    }
}
