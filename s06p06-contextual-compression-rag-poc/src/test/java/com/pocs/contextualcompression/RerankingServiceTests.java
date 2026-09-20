package com.pocs.contextualcompression;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fully offline: {@link DeterministicScoringChatModel} scores by query/passage word overlap
 * instead of calling a real model, but exercises the real prompt-building and score-parsing
 * logic in {@link RerankingService}.
 */
class RerankingServiceTests {

    private final RerankingService rerankingService = new RerankingService(new DeterministicScoringChatModel());

    @Test
    void rerankSortsMostRelevantDocumentFirst() {
        Document onTopic = Document.builder()
                .text("Espresso machines force hot water through finely ground coffee under pressure.")
                .build();
        Document offTopic = Document.builder()
                .text("The Roman Senate met to debate provincial governorships.")
                .build();

        List<ScoredDocument> results = rerankingService.rerank("How does espresso coffee brewing work?",
                List.of(offTopic, onTopic));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).document()).isEqualTo(onTopic);
        assertThat(results.get(0).score()).isGreaterThan(results.get(1).score());
    }

    @Test
    void completelyUnrelatedPassageScoresZero() {
        Document unrelated = Document.builder().text("Zebras and giraffes roam the savanna.").build();

        List<ScoredDocument> results = rerankingService.rerank("How do I brew pour-over coffee?",
                List.of(unrelated));

        assertThat(results.get(0).score()).isZero();
    }
}
