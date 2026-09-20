package com.pocs.contextualcompression;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Console-only demo: with both {@code ANTHROPIC_API_KEY} and {@code GEMINI_API_KEY} set, ingests the sample corpus, over-retrieves candidates for a query, reranks + filters them with a real Claude call, then shows how many chunks fit under the Claude-class vs. */
@Component
public class ContextualCompressionDemoRunner implements CommandLineRunner {

    private static final String QUERY = "How does retrieval-augmented generation work?";

    private final VectorStore vectorStore;
    private final ContextualCompressionService contextualCompressionService;

    public ContextualCompressionDemoRunner(VectorStore vectorStore,
            ContextualCompressionService contextualCompressionService) {
        this.vectorStore = vectorStore;
        this.contextualCompressionService = contextualCompressionService;
    }

    @Override
    public void run(String... args) {
        boolean hasAnthropicKey = System.getenv("ANTHROPIC_API_KEY") != null
                && !System.getenv("ANTHROPIC_API_KEY").isBlank();
        boolean hasGeminiKey = System.getenv("GEMINI_API_KEY") != null && !System.getenv("GEMINI_API_KEY").isBlank();

        if (!hasAnthropicKey || !hasGeminiKey) {
            System.out.println("""
                    Skipping live demo: set both ANTHROPIC_API_KEY and GEMINI_API_KEY to run it.
                    (ANTHROPIC_API_KEY scores/reranks candidates; GEMINI_API_KEY embeds the corpus.)
                    All of this POC's logic is already proven offline by `mvn test` using fakes.
                    """);
            return;
        }

        List<Document> chunks = SampleCorpus.chunkedDocuments();
        vectorStore.add(chunks);
        System.out.println("Ingested " + chunks.size() + " chunks across 3 topics.");

        List<Document> candidates = contextualCompressionService.retrieveCandidates(QUERY);
        System.out.println("Over-retrieved " + candidates.size() + " candidates for query: \"" + QUERY + "\"");

        List<Document> rerankedAndFiltered = contextualCompressionService.rerankAndFilter(QUERY, candidates);
        System.out.println("After cross-encoder-style reranking + relevance filtering: "
                + rerankedAndFiltered.size() + " chunks kept.");
        rerankedAndFiltered.forEach(doc -> System.out.println("  score="
                + doc.getMetadata().get(RelevanceThresholdPostProcessor.RELEVANCE_SCORE_METADATA_KEY)
                + " -> " + doc.getText().substring(0, Math.min(60, doc.getText().length())).replace("\n", " ") + "..."));

        List<Document> claudeBudgeted = contextualCompressionService.fitToBudget(rerankedAndFiltered,
                TokenBudget.CLAUDE_HAIKU_CONTEXT_BUDGET_TOKENS);
        List<Document> geminiBudgeted = contextualCompressionService.fitToBudget(rerankedAndFiltered,
                TokenBudget.GEMINI_FLASH_CONTEXT_BUDGET_TOKENS);

        System.out.println("Claude-class budget (" + TokenBudget.CLAUDE_HAIKU_CONTEXT_BUDGET_TOKENS
                + " tokens): " + claudeBudgeted.size() + " chunks included.");
        System.out.println("Gemini-class budget (" + TokenBudget.GEMINI_FLASH_CONTEXT_BUDGET_TOKENS
                + " tokens): " + geminiBudgeted.size() + " chunks included.");
    }
}
