package com.pocs.beanoutputconverter;

import java.util.Map;
import java.util.function.Function;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

/**
 * Demonstrates Spring AI's {@link BeanOutputConverter}:
 * <ul>
 *     <li>{@link #converter} is constructed once for the {@link MovieReview} record.</li>
 *     <li>{@link #buildPrompt(String)} injects {@code converter.getFormat()} into a
 *         {@link PromptTemplate} variable so the model is told the exact JSON schema
 *         to reply with.</li>
 *     <li>{@link #extract(String)} sends that prompt to Claude and hands the raw text
 *         response to {@code converter.convert(String)}.</li>
 *     <li>{@link #extractWithRetry(String, int, Function)} is the
 *         {@code StructuredOutputConverter} retry loop: if {@code convert} throws
 *         because the model replied with malformed/non-JSON text, we re-prompt with
 *         the format instructions repeated (plus the parse error) up to
 *         {@link #MAX_ATTEMPTS} times before giving up.</li>
 * </ul>
 * The retry-loop method takes the "ask the model" step as a {@link Function} so it can
 * be unit-tested with a canned sequence of responses, without a live API call.
 */
@Service
public class MovieReviewExtractionService {

    static final int MAX_ATTEMPTS = 3;

    private final ChatClient chatClient;
    private final BeanOutputConverter<MovieReview> converter = new BeanOutputConverter<>(MovieReview.class);
    private final PromptTemplate promptTemplate = new PromptTemplate("""
            Read the following movie description and extract a structured review of it.

            Movie description:
            {request}

            {format}
            """);

    public MovieReviewExtractionService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /** Renders the prompt template with the request text and the converter's format instructions. */
    public String buildPrompt(String movieDescription) {
        return promptTemplate.render(Map.of(
                "request", movieDescription,
                "format", converter.getFormat()));
    }

    /** Exposes the format instructions Claude receives, e.g. for logging/demo output. */
    public String getFormatInstructions() {
        return converter.getFormat();
    }

    /** Extracts a MovieReview from free-text, calling the live ChatClient with retry-on-malformed-JSON. */
    public MovieReview extract(String movieDescription) {
        return extractWithRetry(movieDescription, MAX_ATTEMPTS,
                prompt -> chatClient.prompt().user(prompt).call().content());
    }

    /**
     * Core retry loop. {@code modelCaller} maps a prompt string to the model's raw text
     * response - in production it's a live Claude call, in tests it's a canned stub.
     * On a parse failure the loop re-prompts, appending the previous parse error so the
     * model can self-correct, until {@code maxAttempts} is exhausted.
     */
    MovieReview extractWithRetry(String movieDescription, int maxAttempts, Function<String, String> modelCaller) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }

        String prompt = buildPrompt(movieDescription);
        Exception lastError = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String promptForAttempt = attempt == 1 ? prompt : reinforcedPrompt(prompt, lastError);
            String raw = modelCaller.apply(promptForAttempt);
            
            try {
                return converter.convert(raw);
            } catch (RuntimeException parseFailure) {
                lastError = parseFailure;
            }
        }

        throw new StructuredOutputParseException(
                "Failed to parse a MovieReview from the model's response after " + maxAttempts + " attempts",
                lastError);
    }

    private String reinforcedPrompt(String originalPrompt, Exception lastError) {
        String reason = lastError != null ? lastError.getMessage() : "the response was not valid JSON";
        return originalPrompt + "\n\nYour previous reply could not be parsed as valid JSON (" + reason
                + "). Reply again with ONLY the raw JSON object matching the schema above - no markdown "
                + "fences, no commentary.";
    }
}
