package com.pocs.ragevaluation;

import java.util.List;

/** A tiny, hand-picked set of question/answer pairs, each answerable directly from one document in {@link KnowledgeBaseConfig#KNOWLEDGE_BASE}. */
record GoldenCase(String question, String groundedAnswer) {
}

final class GoldenDataset {

    static final List<GoldenCase> CASES = List.of(
            new GoldenCase(
                    "When was the Eiffel Tower completed?",
                    "The Eiffel Tower was completed in 1889."),
            new GoldenCase(
                    "What is Spring AI?",
                    "Spring AI is a Spring-idiomatic framework for building AI applications with "
                            + "portable abstractions across model providers."),
            new GoldenCase(
                    "What is the tallest mountain in Africa?",
                    "Mount Kilimanjaro, in Tanzania, is the tallest mountain in Africa."));

    /** An answer that is not supported by any document in the knowledge base at all. */
    static final String UNGROUNDED_ANSWER =
            "The Eiffel Tower was designed by Leonardo da Vinci and completed in the year 1200.";

    private GoldenDataset() {
    }
}
