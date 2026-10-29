package com.pocs.llmasjudgeevaluation;

import java.util.List;

import org.springframework.ai.evaluation.EvaluationResponse;

/** Turns a batch of {@link EvaluationResponse}s into an aggregate pass rate and pass/fail verdict. */
public final class QualityGate {

    /** Minimum fraction of golden-dataset cases that must pass for the gate to pass. */
    public static final double DEFAULT_PASS_RATE_THRESHOLD = 0.80;

    private final double passRate;
    private final double threshold;
    private final int total;
    private final int passed;

    private QualityGate(double passRate, double threshold, int total, int passed) {
        this.passRate = passRate;
        this.threshold = threshold;
        this.total = total;
        this.passed = passed;
    }

    public static QualityGate evaluate(List<EvaluationResponse> responses, double threshold) {
        int total = responses.size();
        long passed = responses.stream().filter(EvaluationResponse::isPass).count();
        double passRate = total == 0 ? 0.0 : (double) passed / total;
        return new QualityGate(passRate, threshold, total, (int) passed);
    }

    public static QualityGate evaluate(List<EvaluationResponse> responses) {
        return evaluate(responses, DEFAULT_PASS_RATE_THRESHOLD);
    }

    public boolean passes() {
        return passRate >= threshold;
    }

    public double passRate() {
        return passRate;
    }

    public int total() {
        return total;
    }

    public int passed() {
        return passed;
    }

    @Override
    public String toString() {
        return "QualityGate[%d/%d passed, rate=%.2f, threshold=%.2f, verdict=%s]"
                .formatted(passed, total, passRate, threshold, passes() ? "PASS" : "FAIL");
    }
}
