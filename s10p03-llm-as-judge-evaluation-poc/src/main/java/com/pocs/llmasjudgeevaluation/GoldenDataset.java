package com.pocs.llmasjudgeevaluation;

import java.util.List;

/** A tiny, hand-picked set of question/answer pairs used by both offline and live judge tests. */
record GoldenCase(String question, String groundedAnswer) {
}

final class GoldenDataset {

    static final List<GoldenCase> CASES = List.of(
            new GoldenCase(
                    "When did the Panama Canal open?",
                    "The Panama Canal opened in 1914, connecting the Atlantic and Pacific Oceans."),
            new GoldenCase(
                    "What does ChatClient's .stream() method return?",
                    "ChatClient's .stream() method returns a reactive Flux of response chunks."),
            new GoldenCase(
                    "What does photosynthesis produce?",
                    "Photosynthesis produces glucose and oxygen from light energy, water, and carbon dioxide."),
            new GoldenCase(
                    "Where is the Great Barrier Reef located?",
                    "The Great Barrier Reef is located off the coast of Queensland, Australia, and is the "
                            + "world's largest coral reef system."));

    /** An answer not supported by any document in the knowledge base at all. */
    static final String UNGROUNDED_ANSWER =
            "The Panama Canal was built by the Roman Empire in the year 500 BC using steam engines.";

    private GoldenDataset() {
    }
}
