package com.pocs.promptstuffingcontext;

import java.util.Map;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Provider-agnostic "prompt stuffing" abstraction: inject a block of retrieved
 * context (as if a vector store or search index had just returned it) into a
 * {@link PromptTemplate} alongside a user question, estimate the combined prompt's
 * token count with a {@link TokenCountEstimator} <em>before</em> sending anything,
 * and - only when that estimate exceeds a configured budget - truncate the context
 * (never the question) until the rendered prompt fits.
 *
 * <p>Knows nothing about ChatClient or any concrete model: it only produces a
 * rendered prompt string plus the bookkeeping ({@link StuffedPrompt}) describing
 * whether truncation kicked in. That output is exactly what a caller (see
 * {@link ContextStuffingDemoRunner}) hands to any provider's ChatClient.
 */
@Service
public class ContextStuffingService {

    private static final String ANSWER_TEMPLATE = """
            You are a helpful assistant. Use the following retrieved context to \
            answer the question. If the context doesn't contain the answer, say so \
            plainly rather than guessing.

            Context:
            {context}

            Question: {question}

            Answer:""";

    private final TokenCountEstimator tokenCountEstimator;
    private final int maxPromptTokens;

    public ContextStuffingService(TokenCountEstimator tokenCountEstimator,
                                   @Value("${app.max-prompt-tokens:512}") int maxPromptTokens) {
        this.tokenCountEstimator = tokenCountEstimator;
        this.maxPromptTokens = maxPromptTokens;
    }

    /**
     * Renders {@code retrievedContext} and {@code question} into the answer
     * template, estimates the result's token count, and truncates the context
     * (re-rendering and re-estimating) only if the first estimate is over budget.
     */
    public StuffedPrompt buildPrompt(String retrievedContext, String question) {
        String fullPrompt = render(retrievedContext, question);
        int fullTokens = tokenCountEstimator.estimate(fullPrompt);

        if (fullTokens <= maxPromptTokens) {
            return new StuffedPrompt(fullPrompt, retrievedContext, fullTokens, fullTokens, false);
        }

        String truncatedContext = truncateContextToBudget(retrievedContext, question);
        String truncatedPrompt = render(truncatedContext, question);
        int truncatedTokens = tokenCountEstimator.estimate(truncatedPrompt);

        return new StuffedPrompt(truncatedPrompt, truncatedContext, fullTokens, truncatedTokens, true);
    }

    public int estimateTokens(String text) {
        return tokenCountEstimator.estimate(text);
    }

    public int maxPromptTokens() {
        return maxPromptTokens;
    }

    private String truncateContextToBudget(String context, String question) {
        String candidate = context;
        int guard = 0;
        while (guard++ < 50 && !candidate.isEmpty()) {
            String rendered = render(candidate, question);
            int tokens = tokenCountEstimator.estimate(rendered);
            if (tokens <= maxPromptTokens) {
                return candidate;
            }
            double overshoot = (double) tokens / maxPromptTokens;
            int targetLength = (int) (candidate.length() / overshoot * 0.9);
            targetLength = Math.max(0, Math.min(targetLength, candidate.length() - 1));
            candidate = candidate.substring(0, targetLength).stripTrailing();
        }
        return candidate;
    }

    private String render(String context, String question) {
        PromptTemplate template = PromptTemplate.builder().template(ANSWER_TEMPLATE).build();
        return template.render(Map.of("context", context, "question", question));
    }

    public record StuffedPrompt(String renderedPrompt, String contextUsed, int originalTokenCount,
                                 int finalTokenCount, boolean truncated) {
    }
}
