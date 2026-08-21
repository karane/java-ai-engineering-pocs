package com.pocs.beanoutputconverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MovieReviewDemoRunner implements CommandLineRunner {

    private final MovieReviewExtractionService extractionService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String apiKey;

    public MovieReviewDemoRunner(MovieReviewExtractionService extractionService) {
        this.extractionService = extractionService;
    }

    @Override
    public void run(String... args) {
        System.out.println("[s03p01] BeanOutputConverter format instructions injected into the prompt:\n"
                + extractionService.getFormatInstructions());

        if (apiKey.isBlank()) {
            System.out.println("[s03p01] ANTHROPIC_API_KEY not set - skipping live extraction demo. "
                    + "Export ANTHROPIC_API_KEY and re-run to see Claude's free text turned into a MovieReview record.");
            return;
        }

        try {
            String description = "A team of scientists accidentally open a portal to a parallel dimension "
                    + "in 1997, and must close it before it destroys their small coastal town. Tense, "
                    + "atmospheric sci-fi horror with a breakout lead performance.";
            MovieReview review = extractionService.extract(description);
            System.out.println("[s03p01] Extracted MovieReview: " + review);
        } catch (StructuredOutputParseException e) {
            System.out.println("[s03p01] Demo call to Claude failed to produce parseable JSON after retries, "
                    + "but the app started fine: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("[s03p01] Demo call to Claude failed, but the app started fine: " + e.getMessage());
        }
    }
}
