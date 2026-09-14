package com.pocs.ragquality;

import java.util.List;

/** The golden dataset the harness scores both providers against: one question per case, plus the small set of facts (drawn straight from {@link KnowledgeBaseConfig}'s documents) a genuinely grounded answer must contain. */
final class GoldenDataset {

    private GoldenDataset() {
    }

    record QaCase(String question, List<String> expectedFacts) {
    }

    static final List<QaCase> CASES = List.of(
            new QaCase(
                    "Who founded Aurora Notes and when?",
                    List.of("Priya Malhotra", "2019")),
            new QaCase(
                    "What platforms is Aurora Notes available on?",
                    List.of("Windows", "macOS", "Linux", "iOS", "Android")),
            new QaCase(
                    "How much does the Aurora Notes Pro plan cost?",
                    List.of("$6")),
            new QaCase(
                    "Does the free plan include end-to-end encryption?",
                    List.of("Pro subscribers", "unencrypted")),
            new QaCase(
                    "When was real-time collaborative editing added to Aurora Notes, and how many people "
                            + "can edit at once?",
                    List.of("4.0", "March 2023", "10")),
            new QaCase(
                    "Does Aurora Notes offer a public API?",
                    List.of("does not offer a public API", "Zapier")),
            new QaCase(
                    "How does Aurora Notes' offline mode work?",
                    List.of("30 days")),
            new QaCase(
                    "Where is Aurora Notes headquartered and how many employees does it have?",
                    List.of("Lisbon", "22 employees")));
}
