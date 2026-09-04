package com.pocs.embeddingmodel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

/** Unit-level assertions on the pure-math helper, needing no Spring context or live call. */
class EmbeddingServiceTests {

    private final EmbeddingService embeddingService = new EmbeddingService(null);

    @Test
    void cosineSimilarityOfIdenticalVectorsIsOne() {
        float[] v = {1f, 2f, 3f};
        assertThat(embeddingService.cosineSimilarity(v, v)).isCloseTo(1.0, offset(1e-9));
    }

    @Test
    void cosineSimilarityOfOrthogonalVectorsIsZero() {
        float[] a = {1f, 0f};
        float[] b = {0f, 1f};
        assertThat(embeddingService.cosineSimilarity(a, b)).isCloseTo(0.0, offset(1e-9));
    }

    @Test
    void cosineSimilarityRejectsMismatchedLengths() {
        float[] a = {1f, 2f};
        float[] b = {1f, 2f, 3f};
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> embeddingService.cosineSimilarity(a, b));
    }
}
