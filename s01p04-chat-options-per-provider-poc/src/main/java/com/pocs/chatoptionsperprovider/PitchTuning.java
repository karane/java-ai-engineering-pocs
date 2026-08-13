package com.pocs.chatoptionsperprovider;

public record PitchTuning(double creativity, double topP, int topK, int maxTokens) {

    public static PitchTuning conservative() {
        return new PitchTuning(0.1, 0.5, 10, 64);
    }

    public static PitchTuning creative() {
        return new PitchTuning(0.9, 0.95, 40, 256);
    }
}
