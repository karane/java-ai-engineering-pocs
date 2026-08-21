package com.pocs.beanoutputconverter;

import org.junit.jupiter.api.Test;
import org.springframework.ai.converter.BeanOutputConverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MovieReviewBeanOutputConverterTests {

    private final BeanOutputConverter<MovieReview> converter = new BeanOutputConverter<>(MovieReview.class);

    @Test
    void getFormatDescribesAJsonSchemaForMovieReview() {
        String format = converter.getFormat();

        assertThat(format).containsIgnoringCase("json");
        assertThat(format).contains("title");
        assertThat(format).contains("genres");
    }

    @Test
    void convertParsesValidJsonIntoTheRecord() {
        String json = """
                {"title":"Nightfall","releaseYear":2019,"ratingOutOfFive":3.5,
                 "summary":"A detective chases a smuggler through a fog-bound port city.",
                 "genres":["noir","thriller"]}
                """;

        MovieReview review = converter.convert(json);

        assertThat(review).isEqualTo(new MovieReview(
                "Nightfall", 2019, 3.5,
                "A detective chases a smuggler through a fog-bound port city.",
                java.util.List.of("noir", "thriller")));
    }

    @Test
    void convertThrowsOnMalformedJson() {
        assertThatThrownBy(() -> converter.convert("this is not json"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void convertToleratesJsonWrappedInMarkdownFences() {
        String fenced = """
                ```json
                {"title":"Echo","releaseYear":2021,"ratingOutOfFive":4.0,
                 "summary":"A soundless village discovers where the silence came from.",
                 "genres":["mystery"]}
                ```
                """;

        MovieReview review = converter.convert(fenced);

        assertThat(review.title()).isEqualTo("Echo");
    }
}
