package com.pocs.beanoutputconverter;

/**
 * Thrown when {@link MovieReviewExtractionService} exhausts its retry budget
 * without ever producing JSON that {@link org.springframework.ai.converter.BeanOutputConverter}
 * can successfully parse into a {@link MovieReview}.
 */
public class StructuredOutputParseException extends RuntimeException {

    public StructuredOutputParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
