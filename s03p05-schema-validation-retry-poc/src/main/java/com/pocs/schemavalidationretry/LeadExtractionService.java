package com.pocs.schemavalidationretry;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LeadExtractionService {

    private static final Logger log = LoggerFactory.getLogger(LeadExtractionService.class);

    static final int DEFAULT_MAX_ATTEMPTS = 3;

    private final Function<String, String> llmCaller;
    private final Validator validator;
    private final int maxAttempts;
    private final BeanOutputConverter<CustomerLead> converter = new BeanOutputConverter<>(CustomerLead.class);

    @Autowired
    public LeadExtractionService(ChatClient chatClient, Validator validator) {
        this(prompt -> chatClient.prompt(prompt).call().content(), validator, DEFAULT_MAX_ATTEMPTS);
    }

    LeadExtractionService(Function<String, String> llmCaller, Validator validator, int maxAttempts) {
        this.llmCaller = llmCaller;
        this.validator = validator;
        this.maxAttempts = maxAttempts;
    }

    public ExtractionResult extractLead(String rawInquiry) {
        String basePrompt = """
                Extract structured customer lead information from the raw sales inquiry below.
                Also self-report a confidenceScore between 0.0 and 1.0 reflecting how confident
                you are that the fields you extracted are accurate and complete.

                Raw inquiry:
                %s

                %s
                """.formatted(rawInquiry, converter.getFormat());

        String prompt = basePrompt;
        Set<ConstraintViolation<CustomerLead>> lastViolations = Set.of();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            log.info("Attempt {}/{}: raw prompt sent to ChatClient:\n{}", attempt, maxAttempts, prompt);
            String rawResponse = llmCaller.apply(prompt);
            log.info("Attempt {}/{}: raw response from ChatClient:\n{}", attempt, maxAttempts, rawResponse);

            CustomerLead lead;
            try {
                lead = converter.convert(rawResponse);
            } catch (RuntimeException parseFailure) {
                log.warn("Attempt {}/{}: response was not parseable JSON: {}", attempt, maxAttempts,
                        parseFailure.getMessage());
                prompt = basePrompt + """


                        Your previous response could not be parsed as JSON matching the required
                        schema (%s). Respond with valid JSON only, no prose, no markdown fences.
                        """.formatted(parseFailure.getMessage());
                continue;
            }

            Set<ConstraintViolation<CustomerLead>> violations = validator.validate(lead);
            if (violations.isEmpty()) {
                return new ExtractionResult(lead, attempt);
            }

            lastViolations = violations;
            String violationSummary = describe(violations);
            log.warn("Attempt {}/{}: validation failed: {}", attempt, maxAttempts, violationSummary);

            prompt = basePrompt + """


                    Your previous response violated these validation rules:
                    %s

                    Correct every listed field and respond again with valid JSON only.
                    """.formatted(violationSummary);
        }

        throw new SchemaValidationExhaustedException(maxAttempts, lastViolations);
    }

    private static String describe(Set<ConstraintViolation<CustomerLead>> violations) {
        return violations.stream()
                .map(v -> "- " + v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("\n"));
    }
}
