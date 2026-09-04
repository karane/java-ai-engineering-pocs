package com.pocs.embeddingmodel;

import java.util.List;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /** Embeds a single piece of text into a float vector. */
    public float[] embed(String text) {
        return embeddingModel.embed(text);
    }

    /** Embeds a batch of texts, returning the full response including usage metadata. */
    public EmbeddingResponse embedForResponse(List<String> texts) {
        return embeddingModel.embedForResponse(texts);
    }

    /** The vector size produced by the underlying model (768 for text-embedding-004). */
    public int dimensions() {
        return embeddingModel.dimensions();
    }

    /** Cosine similarity between two embedding vectors of equal length. */
    public double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vectors must be the same length");
        }
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
