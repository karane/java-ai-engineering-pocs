package com.pocs.embeddingmodel;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class EmbeddingServiceLiveTest {

    @Autowired
    private EmbeddingService embeddingService;

    @Test
    void embedReturnsA768DimensionVector() {
        float[] vector = embeddingService.embed("Spring AI makes building AI apps easier.");
        assertThat(vector).isNotEmpty();
        assertThat(embeddingService.dimensions()).isEqualTo(vector.length);
    }

    @Test
    void embedForResponseExposesUsageMetadataAndSimilarTextsScoreHigher() {
        EmbeddingResponse response = embeddingService.embedForResponse(
                List.of("The cat sat on the mat.", "A feline rested on the rug.", "Quantum computers use qubits."));

        assertThat(response.getResults()).hasSize(3);
        assertThat(response.getMetadata().getModel()).isNotBlank();

        float[] cat = response.getResults().get(0).getOutput();
        float[] feline = response.getResults().get(1).getOutput();
        float[] quantum = response.getResults().get(2).getOutput();

        double catFelineSimilarity = embeddingService.cosineSimilarity(cat, feline);
        double catQuantumSimilarity = embeddingService.cosineSimilarity(cat, quantum);

        assertThat(catFelineSimilarity).isGreaterThan(catQuantumSimilarity);
    }
}
