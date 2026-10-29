package com.pocs.llmasjudgeevaluation;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ai.evaluation.EvaluationResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link QualityGate} - pure arithmetic over hand-built {@link EvaluationResponse}
 * objects, no Spring context, no network.
 */
class QualityGateTests {

    private static EvaluationResponse response(boolean pass) {
        return new EvaluationResponse(pass, pass ? 1.0f : 0.0f, "", Map.of());
    }

    @Test
    void allPassingResponsesClearTheDefaultThreshold() {
        QualityGate gate = QualityGate.evaluate(List.of(response(true), response(true), response(true)));

        assertThat(gate.passes()).isTrue();
        assertThat(gate.passRate()).isEqualTo(1.0);
        assertThat(gate.total()).isEqualTo(3);
        assertThat(gate.passed()).isEqualTo(3);
    }

    @Test
    void exactlyAtThresholdPasses() {
        // 4/5 = 0.80, equal to the default threshold - should pass, not fail, on a boundary.
        QualityGate gate = QualityGate.evaluate(
                List.of(response(true), response(true), response(true), response(true), response(false)));

        assertThat(gate.passRate()).isEqualTo(0.80);
        assertThat(gate.passes()).isTrue();
    }

    @Test
    void belowThresholdFailsTheGate() {
        QualityGate gate = QualityGate.evaluate(
                List.of(response(true), response(false), response(false)));

        assertThat(gate.passRate()).isCloseTo(0.333, org.assertj.core.data.Offset.offset(0.001));
        assertThat(gate.passes()).isFalse();
    }

    @Test
    void customThresholdIsRespected() {
        QualityGate strictGate = QualityGate.evaluate(List.of(response(true), response(false)), 0.90);
        QualityGate lenientGate = QualityGate.evaluate(List.of(response(true), response(false)), 0.50);

        assertThat(strictGate.passes()).isFalse();
        assertThat(lenientGate.passes()).isTrue();
    }

    @Test
    void emptyResponseListHasZeroPassRateAndFailsTheGate() {
        QualityGate gate = QualityGate.evaluate(List.of());

        assertThat(gate.total()).isZero();
        assertThat(gate.passRate()).isZero();
        assertThat(gate.passes()).isFalse();
    }
}
