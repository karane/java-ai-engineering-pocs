package com.pocs.beanoutputconverter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MovieReviewExtractionServiceTests {

    private static final String VALID_JSON = """
            {"title":"Parallel","releaseYear":1997,"ratingOutOfFive":4.5,
             "summary":"Scientists race to close a rift before it swallows their town.",
             "genres":["sci-fi","horror"]}
            """;

    private final MovieReviewExtractionService service = new MovieReviewExtractionService(null);

    @Test
    void buildPromptInjectsRequestAndFormatInstructions() {
        String prompt = service.buildPrompt("A heist film set in Lisbon.");

        assertThat(prompt).contains("A heist film set in Lisbon.");
        assertThat(prompt).contains(service.getFormatInstructions());
        // BeanOutputConverter's default format instructions describe a JSON schema.
        assertThat(service.getFormatInstructions()).containsIgnoringCase("json");
    }

    @Test
    void extractWithRetrySucceedsOnFirstValidResponse() {
        Function<String, String> modelCaller = fixedResponses(List.of(VALID_JSON));

        MovieReview review = service.extractWithRetry("A heist film set in Lisbon.", 3, modelCaller);

        assertThat(review.title()).isEqualTo("Parallel");
        assertThat(review.releaseYear()).isEqualTo(1997);
        assertThat(review.ratingOutOfFive()).isEqualTo(4.5);
        assertThat(review.genres()).containsExactly("sci-fi", "horror");
    }

    @Test
    void extractWithRetryRecoversAfterMalformedJsonOnce() {
        Function<String, String> modelCaller = fixedResponses(List.of(
                "Sure! Here's the review: " + VALID_JSON, // not raw JSON, first attempt fails
                VALID_JSON // second attempt is clean
        ));

        MovieReview review = service.extractWithRetry("A heist film set in Lisbon.", 3, modelCaller);

        assertThat(review.title()).isEqualTo("Parallel");
    }

    @Test
    void extractWithRetryGivesUpAfterExhaustingAttempts() {
        Function<String, String> modelCaller = fixedResponses(List.of(
                "not json at all",
                "still not json",
                "nope, still text"
        ));

        assertThatThrownBy(() -> service.extractWithRetry("A heist film set in Lisbon.", 3, modelCaller))
                .isInstanceOf(StructuredOutputParseException.class)
                .hasMessageContaining("3 attempts");
    }

    @Test
    void extractWithRetryRejectsNonPositiveMaxAttempts() {
        assertThatThrownBy(() -> service.extractWithRetry("x", 0, prompt -> VALID_JSON))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** Returns a Function that hands back each queued response in order, one per invocation. */
    private static Function<String, String> fixedResponses(List<String> responses) {
        Deque<String> queue = new ArrayDeque<>(responses);
        return prompt -> {
            if (queue.isEmpty()) {
                throw new IllegalStateException("No more canned responses queued");
            }
            return queue.poll();
        };
    }
}
