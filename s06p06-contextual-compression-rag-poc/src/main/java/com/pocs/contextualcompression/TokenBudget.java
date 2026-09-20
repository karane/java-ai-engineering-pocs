package com.pocs.contextualcompression;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.document.Document;

/** Trims an already-reranked/filtered document list so it fits inside a token budget before it's injected into a prompt as RAG context. */
public final class TokenBudget {

    private static final double CHARS_PER_TOKEN = 4.0;

    /** Real full context window of s01p01's claude-haiku-4-5-20251001, for reference. */
    public static final int CLAUDE_HAIKU_CONTEXT_WINDOW_TOKENS = 200_000;

    /** Real full context window of s01p02's gemini-3.5-flash-lite, for reference. */
    public static final int GEMINI_FLASH_CONTEXT_WINDOW_TOKENS = 1_000_000;

    /**
     * This POC's demo-scaled slice of {@link #CLAUDE_HAIKU_CONTEXT_WINDOW_TOKENS} reserved for
     * injected RAG context - small enough that a handful of demo chunks can exceed it.
     */
    public static final int CLAUDE_HAIKU_CONTEXT_BUDGET_TOKENS = 200;

    /**
     * This POC's demo-scaled slice of {@link #GEMINI_FLASH_CONTEXT_WINDOW_TOKENS} reserved for
     * injected RAG context - five times {@link #CLAUDE_HAIKU_CONTEXT_BUDGET_TOKENS}, the same
     * ratio as the two real windows above.
     */
    public static final int GEMINI_FLASH_CONTEXT_BUDGET_TOKENS = 1_000;

    private TokenBudget() {
    }

    /** Approximate token count for {@code text}, using the chars-per-4 heuristic. */
    public static int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return (int) Math.ceil(text.length() / CHARS_PER_TOKEN);
    }

    /** Greedily selects documents from {@code rankedDocuments}, in order, until adding the next one would exceed {@code budgetTokens}. */
    public static List<Document> selectWithinBudget(List<Document> rankedDocuments, int budgetTokens) {
        List<Document> selected = new ArrayList<>();
        int usedTokens = 0;
        for (Document document : rankedDocuments) {
            int documentTokens = estimateTokens(document.getText());
            if (usedTokens + documentTokens > budgetTokens) {
                break;
            }
            selected.add(document);
            usedTokens += documentTokens;
        }
        return selected;
    }
}
