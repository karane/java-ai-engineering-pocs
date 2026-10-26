package com.pocs.tokenusagecosttracking;

/** The two providers compared by this POC, carrying their own per-1k-token pricing constants. */
public enum Provider {

    ANTHROPIC("anthropic", "claude-haiku-4-5-20251001", 0.0010, 0.0050),
    GEMINI("gemini", "gemini-3.5-flash-lite", 0.00010, 0.00040);

    private final String tag;
    private final String model;
    private final double promptPricePerThousandTokensUsd;
    private final double completionPricePerThousandTokensUsd;

    Provider(String tag, String model, double promptPricePerThousandTokensUsd,
            double completionPricePerThousandTokensUsd) {
        this.tag = tag;
        this.model = model;
        this.promptPricePerThousandTokensUsd = promptPricePerThousandTokensUsd;
        this.completionPricePerThousandTokensUsd = completionPricePerThousandTokensUsd;
    }

    public String tag() {
        return tag;
    }

    public String model() {
        return model;
    }

    public double promptPricePerThousandTokensUsd() {
        return promptPricePerThousandTokensUsd;
    }

    public double completionPricePerThousandTokensUsd() {
        return completionPricePerThousandTokensUsd;
    }
}
