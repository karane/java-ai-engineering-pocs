package com.pocs.llmasjudgeevaluation;

import java.util.Map;

import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.Evaluator;

/** A custom {@link Evaluator} that scores an answer purely by word count, with no LLM call. */
public class ConcisenessEvaluator implements Evaluator {

    private final int maxWords;

    public ConcisenessEvaluator(int maxWords) {
        this.maxWords = maxWords;
    }

    @Override
    public EvaluationResponse evaluate(EvaluationRequest evaluationRequest) {
        String response = evaluationRequest.getResponseContent();
        int wordCount = response == null || response.isBlank() ? 0 : response.trim().split("\\s+").length;
        boolean pass = wordCount <= maxWords;
        float score = pass ? 1.0f : 0.0f;
        String feedback = pass
                ? "Answer is %d words, within the %d-word budget.".formatted(wordCount, maxWords)
                : "Answer is %d words, exceeding the %d-word budget.".formatted(wordCount, maxWords);
        return new EvaluationResponse(pass, score, feedback, Map.of("wordCount", wordCount, "maxWords", maxWords));
    }
}
