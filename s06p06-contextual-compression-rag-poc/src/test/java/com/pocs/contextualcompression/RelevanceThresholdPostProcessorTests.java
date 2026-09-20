package com.pocs.contextualcompression;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fully offline, using {@link DeterministicScoringChatModel} so scores are real word-overlap
 * numbers rather than one fixed canned value - enough to prove the threshold actually filters,
 * and that surviving documents both get reordered by score and carry it as metadata.
 */
class RelevanceThresholdPostProcessorTests {

    private final RerankingService rerankingService = new RerankingService(new DeterministicScoringChatModel());

    @Test
    void dropsDocumentsBelowTheThresholdAndKeepsTheRest() {
        RelevanceThresholdPostProcessor postProcessor = new RelevanceThresholdPostProcessor(rerankingService, 3.0);

        Document relevant = Document.builder()
                .text("Retrieval augmented generation retrieves context chunks before generation.")
                .build();
        Document irrelevant = Document.builder().text("Pour-over coffee needs a paper filter.").build();

        List<Document> result = postProcessor.process(new Query("How does retrieval augmented generation work?"),
                List.of(irrelevant, relevant));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getText()).isEqualTo(relevant.getText());
    }

    @Test
    void survivingDocumentsCarryTheirRelevanceScoreAsMetadata() {
        RelevanceThresholdPostProcessor postProcessor = new RelevanceThresholdPostProcessor(rerankingService, 0.0);
        Document document = Document.builder().text("Spring AI wraps chat and embedding model providers.").build();

        List<Document> result = postProcessor.process(new Query("What is Spring AI?"), List.of(document));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMetadata())
                .containsKey(RelevanceThresholdPostProcessor.RELEVANCE_SCORE_METADATA_KEY);
    }

    @Test
    void resultsAreSortedByDescendingRelevance() {
        RelevanceThresholdPostProcessor postProcessor = new RelevanceThresholdPostProcessor(rerankingService, 0.0);

        Document highlyRelevant = Document.builder()
                .text("Espresso espresso espresso brewing pressure espresso.")
                .build();
        Document mildlyRelevant = Document.builder().text("Espresso is a coffee brewing method.").build();

        List<Document> result = postProcessor.process(new Query("espresso brewing pressure"),
                List.of(mildlyRelevant, highlyRelevant));

        assertThat(result.get(0).getText()).isEqualTo(highlyRelevant.getText());
    }
}
