package com.pocs.fewshotprompting;

import java.util.List;

/**
 * One labeled example for the few-shot classification task: classify a short
 * piece of customer-support text into exactly one of a fixed set of categories
 * (billing / bug / feature-request / praise). Each example becomes one
 * UserMessage/AssistantMessage pair in the few-shot Prompt, and one row in the
 * PromptTemplate loop that renders them into a single templated string.
 */
public record ClassificationExample(String text, String label) {

    public static List<ClassificationExample> catalog() {
        return List.of(
                new ClassificationExample("I was charged twice for my subscription this month.", "billing"),
                new ClassificationExample("The app crashes every time I try to export a PDF.", "bug"),
                new ClassificationExample("It would be great if you added dark mode support.", "feature-request"),
                new ClassificationExample("Your support team resolved my issue in minutes, fantastic service!", "praise")
        );
    }
}
