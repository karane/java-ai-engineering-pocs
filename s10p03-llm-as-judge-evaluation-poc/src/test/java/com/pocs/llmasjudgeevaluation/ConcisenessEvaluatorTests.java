package com.pocs.llmasjudgeevaluation;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the custom, non-LLM {@link ConcisenessEvaluator} - no Spring context, no network, no API
 * key. Proves the {@code Evaluator} extension point works for a purely deterministic check.
 */
class ConcisenessEvaluatorTests {

    @Test
    void shortAnswerPasses() {
        ConcisenessEvaluator evaluator = new ConcisenessEvaluator(10);

        EvaluationResponse response = evaluator.evaluate(
                new EvaluationRequest("question", List.of(), "a short five word answer here"));

        assertThat(response.isPass()).isTrue();
        assertThat(response.getScore()).isEqualTo(1.0f);
    }

    @Test
    void answerOverTheBudgetFails() {
        ConcisenessEvaluator evaluator = new ConcisenessEvaluator(3);

        EvaluationResponse response = evaluator.evaluate(
                new EvaluationRequest("question", List.of(), "this answer has way more than three words in it"));

        assertThat(response.isPass()).isFalse();
        assertThat(response.getScore()).isEqualTo(0.0f);
    }

    @Test
    void answerExactlyAtTheBudgetPasses() {
        ConcisenessEvaluator evaluator = new ConcisenessEvaluator(3);

        EvaluationResponse response = evaluator.evaluate(
                new EvaluationRequest("question", List.of(), "one two three"));

        assertThat(response.isPass()).isTrue();
    }

    @Test
    void blankAnswerCountsAsZeroWordsAndPasses() {
        ConcisenessEvaluator evaluator = new ConcisenessEvaluator(0);

        EvaluationResponse response = evaluator.evaluate(new EvaluationRequest("question", List.of(), "   "));

        assertThat(response.isPass()).isTrue();
        assertThat(response.getMetadata()).containsEntry("wordCount", 0);
    }
}
