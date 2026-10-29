package com.pocs.llmasjudgeevaluation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

/** A deterministic, dependency-free {@link EmbeddingModel} using a bag-of-words hashing trick. */
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
            int bucket = Math.floorMod(token.hashCode(), DIMENSIONS);
            vector[bucket] += 1f;
        }
        return vector;
    }
}
