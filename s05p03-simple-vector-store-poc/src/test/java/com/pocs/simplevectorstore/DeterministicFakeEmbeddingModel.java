package com.pocs.simplevectorstore;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

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
            // floorMod (not %) keeps every component non-negative regardless of the sign
            // of String.hashCode() - using plain % here originally let two different
            // texts land as near-opposite vectors (cosine ~ -1), which similaritySearch
            // then silently filtered out via its default [0,1] similarity threshold.
            int seed = Math.floorMod(text.hashCode() + i * 7919, 1000);
            vector[i] = seed / 1000f;
        }
        return vector;
    }
}
