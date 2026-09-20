package com.pocs.contextualcompression;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/** Runs the full contextual-compression pipeline: retrieve, rerank/filter, then trim to a token budget. */
@Service
public class ContextualCompressionService {

    /** Deliberately over-retrieves - most of these candidates are expected to get filtered out. */
    public static final int OVER_RETRIEVAL_TOP_K = 10;

    private final VectorStore vectorStore;
    private final RelevanceThresholdPostProcessor relevanceThresholdPostProcessor;

    public ContextualCompressionService(VectorStore vectorStore,
            RelevanceThresholdPostProcessor relevanceThresholdPostProcessor) {
        this.vectorStore = vectorStore;
        this.relevanceThresholdPostProcessor = relevanceThresholdPostProcessor;
    }

    /** Stage 1: over-retrieve a wide, unfiltered candidate set by similarity search alone. */
    public List<Document> retrieveCandidates(String query) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(OVER_RETRIEVAL_TOP_K)
                .similarityThreshold(SearchRequest.SIMILARITY_THRESHOLD_ACCEPT_ALL)
                .build());
    }

    /** Stage 2: rerank the candidates and drop the ones below the relevance threshold. */
    public List<Document> rerankAndFilter(String query, List<Document> candidates) {
        return relevanceThresholdPostProcessor.process(new Query(query), candidates);
    }

    /** Stage 3: trim the reranked/filtered chunks to fit {@code budgetTokens}. */
    public List<Document> fitToBudget(List<Document> rerankedAndFiltered, int budgetTokens) {
        return TokenBudget.selectWithinBudget(rerankedAndFiltered, budgetTokens);
    }

    /** Runs all three stages in sequence and returns only the final, budgeted chunk list. */
    public List<Document> compress(String query, int budgetTokens) {
        List<Document> candidates = retrieveCandidates(query);
        List<Document> rerankedAndFiltered = rerankAndFilter(query, candidates);
        return fitToBudget(rerankedAndFiltered, budgetTokens);
    }
}
