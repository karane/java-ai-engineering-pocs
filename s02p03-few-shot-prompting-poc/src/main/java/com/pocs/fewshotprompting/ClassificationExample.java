package com.pocs.fewshotprompting;

import java.util.List;

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
