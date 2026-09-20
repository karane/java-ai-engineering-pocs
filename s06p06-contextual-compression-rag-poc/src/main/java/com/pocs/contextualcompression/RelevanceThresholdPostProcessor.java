package com.pocs.contextualcompression;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;

/** A {@link DocumentPostProcessor} that reranks the retrieved candidates via {@link RerankingService} and drops every chunk scoring below {@code minScore}. */
public class RelevanceThresholdPostProcessor implements DocumentPostProcessor {

    public static final String RELEVANCE_SCORE_METADATA_KEY = "relevance_score";

    private final RerankingService rerankingService;
    private final double minScore;

    public RelevanceThresholdPostProcessor(RerankingService rerankingService, double minScore) {
        this.rerankingService = rerankingService;
        this.minScore = minScore;
    }

    @Override
    public List<Document> process(Query query, List<Document> documents) {
        return rerankingService.rerank(query.text(), documents).stream()
                .filter(scored -> scored.score() >= minScore)
                .map(scored -> scored.document().mutate()
                        .metadata(RELEVANCE_SCORE_METADATA_KEY, scored.score())
                        .build())
                .toList();
    }
}
