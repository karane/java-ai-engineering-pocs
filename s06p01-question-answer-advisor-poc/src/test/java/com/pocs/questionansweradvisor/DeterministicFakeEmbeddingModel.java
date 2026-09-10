package com.pocs.questionansweradvisor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

/** Trivial hash-based {@link EmbeddingModel} for offline tests - no network call or API key. */
class DeterministicFakeEmbeddingModel implements EmbeddingModel {

    static final int DIMENSIONS = 8;

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
        for (int i = 0; i < DIMENSIONS; i++) {
            int seed = Math.floorMod(text.hashCode() + i * 7919, 1000);
            vector[i] = seed / 1000f;
        }
        return vector;
    }
}
