package com.pocs.contextualcompression;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;

import static org.assertj.core.api.Assertions.assertThat;

/** End-to-end, fully offline: a real {@link SimpleVectorStore} backed by a deterministic fake embedding model does the over-retrieval, and a real {@link RerankingService} backed by {@link DeterministicScoringChatModel} does the reranking/filtering. */
class ContextualCompressionServiceTests {

    private ContextualCompressionService newService(List<Document> corpus) {
        VectorStore vectorStore = SimpleVectorStore.builder(new DeterministicFakeEmbeddingModel()).build();
        vectorStore.add(corpus);
        RerankingService rerankingService = new RerankingService(new DeterministicScoringChatModel());
        RelevanceThresholdPostProcessor postProcessor = new RelevanceThresholdPostProcessor(rerankingService, 3.0);
        return new ContextualCompressionService(vectorStore, postProcessor);
    }

    @Test
    void overRetrievesUpToTopKCandidatesRegardlessOfRelevance() {
        List<Document> corpus = SampleCorpus.chunkedDocuments();
        ContextualCompressionService service = newService(corpus);

        List<Document> candidates = service.retrieveCandidates("How does retrieval-augmented generation work?");

        assertThat(candidates.size()).isLessThanOrEqualTo(ContextualCompressionService.OVER_RETRIEVAL_TOP_K);
        assertThat(candidates).isNotEmpty();
    }

    @Test
    void rerankAndFilterDropsTheOffTopicCandidatesFromTheOverRetrievedSet() {
        List<Document> corpus = SampleCorpus.chunkedDocuments();
        ContextualCompressionService service = newService(corpus);
        String query = "How does retrieval-augmented generation work with a vector store?";

        List<Document> candidates = service.retrieveCandidates(query);
        List<Document> filtered = service.rerankAndFilter(query, candidates);

        assertThat(filtered.size()).isLessThan(candidates.size());
        assertThat(filtered).allSatisfy(doc -> assertThat(doc.getMetadata())
                .containsKey(RelevanceThresholdPostProcessor.RELEVANCE_SCORE_METADATA_KEY));
    }

    @Test
    void geminiClassBudgetIncludesAtLeastAsManyChunksAsClaudeClassBudget() {
        List<Document> corpus = SampleCorpus.chunkedDocuments();
        ContextualCompressionService service = newService(corpus);
        String query = "Tell me about Spring AI, Roman history, and coffee brewing";

        List<Document> candidates = service.retrieveCandidates(query);
        List<Document> filtered = service.rerankAndFilter(query, candidates);

        List<Document> claudeBudgeted = service.fitToBudget(filtered, TokenBudget.CLAUDE_HAIKU_CONTEXT_BUDGET_TOKENS);
        List<Document> geminiBudgeted = service.fitToBudget(filtered, TokenBudget.GEMINI_FLASH_CONTEXT_BUDGET_TOKENS);

        assertThat(claudeBudgeted.size()).isLessThanOrEqualTo(geminiBudgeted.size());
    }

    @Test
    void compressRunsAllThreeStagesAndReturnsAResultWithinBudget() {
        List<Document> corpus = SampleCorpus.chunkedDocuments();
        ContextualCompressionService service = newService(corpus);

        List<Document> result = service.compress("How does retrieval-augmented generation work?",
                TokenBudget.CLAUDE_HAIKU_CONTEXT_BUDGET_TOKENS);

        int totalTokens = result.stream().mapToInt(doc -> TokenBudget.estimateTokens(doc.getText())).sum();
        assertThat(totalTokens).isLessThanOrEqualTo(TokenBudget.CLAUDE_HAIKU_CONTEXT_BUDGET_TOKENS);
    }
}
