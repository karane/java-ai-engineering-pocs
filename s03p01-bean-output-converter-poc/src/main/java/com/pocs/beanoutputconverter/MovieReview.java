package com.pocs.beanoutputconverter;

import java.util.List;

/**
 * The target structured-output shape for this POC. Deliberately has a nested
 * list field ({@code genres}) and a scalar field with a constrained range
 * ({@code ratingOutOfFive}) so the LLM has to produce a non-trivial JSON
 * object rather than a single flat value, and so BeanOutputConverter's
 * schema generation has something interesting to describe.
 *
 * @param title            the movie's title
 * @param releaseYear      four-digit release year
 * @param ratingOutOfFive  reviewer rating, expected in the 1-5 range
 * @param summary          one or two sentence spoiler-free summary
 * @param genres           list of genre tags, e.g. ["sci-fi", "thriller"]
 */
public record MovieReview(
        String title,
        int releaseYear,
        double ratingOutOfFive,
        String summary,
        List<String> genres) {
}
