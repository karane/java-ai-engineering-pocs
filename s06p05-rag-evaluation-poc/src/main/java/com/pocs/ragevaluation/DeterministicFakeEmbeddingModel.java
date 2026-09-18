package com.pocs.ragevaluation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

/** A deterministic, dependency-free {@link EmbeddingModel} using the classic "hashing trick": each word in the text is lower-cased and hashed into one of a fixed number of buckets, and that bucket's count is incremented. */
public class DeterministicFakeEmbeddingModel implements EmbeddingModel {

    static final int DIMENSIONS = 64;

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        int index = 0;
        for (String text : request.getInstructions()) {
            embeddings.add(new Embedding(vectorFor(text), index++));
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return vectorFor(document.getText());
    }

    private float[] vectorFor(String text) {
        float[] vector = new float[DIMENSIONS];
        for (String token : text.toLowerCase().split("[^a-z0-9]+")) {
            if (token.isBlank()) {
                continue;
            }
            // floorMod (not %) keeps every bucket index non-negative regardless of the
            // sign of String.hashCode() - see s05p03's README for why plain % is a trap.
            int bucket = Math.floorMod(token.hashCode(), DIMENSIONS);
            vector[bucket] += 1f;
        }
        return vector;
    }
}
