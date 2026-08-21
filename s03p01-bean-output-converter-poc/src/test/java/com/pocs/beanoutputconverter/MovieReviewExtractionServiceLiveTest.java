package com.pocs.beanoutputconverter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class MovieReviewExtractionServiceLiveTest {

    @Autowired
    private MovieReviewExtractionService extractionService;

    @Test
    void extractTurnsFreeTextIntoAStructuredMovieReview() {
        String description = "\"Vault of Tides\" (2019): A retired locksmith is pulled into one last job "
                + "breaking into a vault beneath a flooded 1970s casino, in this slow-burn heist thriller.";

        MovieReview review = extractionService.extract(description);

        assertThat(review.title()).isNotBlank();
        assertThat(review.releaseYear()).isGreaterThan(0);
        assertThat(review.summary()).isNotBlank();
        assertThat(review.genres()).isNotEmpty();
    }
}
