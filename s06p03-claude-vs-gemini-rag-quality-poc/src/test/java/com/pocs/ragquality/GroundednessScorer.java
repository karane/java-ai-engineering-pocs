package com.pocs.ragquality;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.pocs.ragquality.GoldenDataset.QaCase;

/** Scores one RAG answer against its {@link QaCase}: groundedness here is proxied as "what fraction of the case's expected facts show up verbatim (case-insensitive) in the answer text". */
final class GroundednessScorer {

    private GroundednessScorer() {
    }

    record Score(QaCase qaCase, String answer, List<String> matchedFacts, List<String> missingFacts) {

        double groundedness() {
            int total = matchedFacts.size() + missingFacts.size();
            return total == 0 ? 1.0 : (double) matchedFacts.size() / total;
        }

        boolean isFullyGrounded() {
            return missingFacts.isEmpty();
        }
    }

    static Score score(QaCase qaCase, String answer) {
        String normalizedAnswer = answer.toLowerCase(Locale.ROOT);
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String fact : qaCase.expectedFacts()) {
            if (normalizedAnswer.contains(fact.toLowerCase(Locale.ROOT))) {
                matched.add(fact);
            } else {
                missing.add(fact);
            }
        }
        return new Score(qaCase, answer, matched, missing);
    }
}
