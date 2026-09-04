package com.pocs.embeddingmodel;

import java.util.List;

import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class EmbeddingDemoRunner implements CommandLineRunner {

    @Autowired
    private EmbeddingService embeddingService;

    @Override
    public void run(String... args) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (!StringUtils.hasText(apiKey)) {
            System.out.println("[s05p01] GEMINI_API_KEY not set — skipping live demo call. "
                    + "Export GEMINI_API_KEY and re-run to see real gemini-embedding-001 vectors.");
            return;
        }
        try {
            System.out.println("[s05p01] Model dimensions: " + embeddingService.dimensions());

            float[] catVector = embeddingService.embed("The cat sat on the mat.");
            System.out.println("[s05p01] Embedded 1 sentence into a " + catVector.length + "-dim vector.");

            EmbeddingResponse response = embeddingService.embedForResponse(
                    List.of("A dog is a loyal animal.", "Quantum computers use qubits."));
            System.out.println("[s05p01] Batch embedForResponse() returned " + response.getResults().size()
                    + " embeddings, model=" + response.getMetadata().getModel()
                    + ", usage=" + response.getMetadata().getUsage());

            float[] dogVector = response.getResults().get(0).getOutput();
            float[] quantumVector = response.getResults().get(1).getOutput();
            System.out.println("[s05p01] cosine(cat, dog) = " + embeddingService.cosineSimilarity(catVector, dogVector));
            System.out.println("[s05p01] cosine(cat, quantum) = " + embeddingService.cosineSimilarity(catVector, quantumVector));
        } catch (RuntimeException e) {
            System.out.println("[s05p01] Demo call failed, but the app started fine: " + e.getMessage());
        }
    }
}
