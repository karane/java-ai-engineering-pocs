package com.pocs.ragquality;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

/** A trivial hash-based {@link EmbeddingModel}, the same idea as s05p03's test-only fake but promoted here to a real {@code main} bean: it is the ONLY embedding model this POC ever uses, for the demo run and for every test. */
public class DeterministicFakeEmbeddingModel implements EmbeddingModel {

    static final int DIMENSIONS = 16;

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
            // floorMod (not %) keeps every component non-negative regardless of the sign of
            // String.hashCode() - see s05p03's DeterministicFakeEmbeddingModel for the same fix.
            int seed = Math.floorMod(text.hashCode() + i * 7919, 1000);
            vector[i] = seed / 1000f;
        }
        return vector;
    }
}
