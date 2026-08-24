package com.pocs.schemavalidationretry;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeadExtractionServiceTest {

    private static final String VALID_JSON = """
            {
              "fullName": "Jane Doe",
              "email": "jane.doe@example.com",
              "phone": "+1 (555) 123-4567",
              "inquirySummary": "Interested in the enterprise pricing tier for a 200-seat team.",
              "confidenceScore": 0.92
            }
            """;

    private static final String INVALID_EMAIL_JSON = """
            {
              "fullName": "Jane Doe",
              "email": "not-an-email",
              "phone": "+1 (555) 123-4567",
              "inquirySummary": "Interested in the enterprise pricing tier for a 200-seat team.",
              "confidenceScore": 0.4
            }
            """;

    private static final String UNPARSEABLE_RESPONSE = "Sure! Here is the lead: Jane Doe, jane@example.com";

    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void succeedsOnFirstAttemptWhenResponseIsAlreadyValid() {
        FakeLlm fakeLlm = FakeLlm.respondingWith(VALID_JSON);
        LeadExtractionService service = new LeadExtractionService(fakeLlm, validator, 3);

        ExtractionResult result = service.extractLead("raw inquiry text");

        assertThat(result.attemptsUsed()).isEqualTo(1);
        assertThat(result.lead().fullName()).isEqualTo("Jane Doe");
        assertThat(result.lead().confidenceScore()).isEqualTo(0.92);
        assertThat(fakeLlm.callCount()).isEqualTo(1);
    }

    @Test
    void reprompsAfterValidationFailureAndSucceedsOnSecondAttempt() {
        FakeLlm fakeLlm = FakeLlm.respondingWith(INVALID_EMAIL_JSON, VALID_JSON);
        LeadExtractionService service = new LeadExtractionService(fakeLlm, validator, 3);

        ExtractionResult result = service.extractLead("raw inquiry text");

        assertThat(result.attemptsUsed()).isEqualTo(2);
        assertThat(result.lead().email()).isEqualTo("jane.doe@example.com");
        assertThat(fakeLlm.callCount()).isEqualTo(2);

        // The second prompt must include the validation error so the model knows what to fix.
        assertThat(fakeLlm.promptAt(1)).contains("violated these validation rules").contains("email");
    }

    @Test
    void reprompsAfterUnparseableJsonAndSucceedsOnSecondAttempt() {
        FakeLlm fakeLlm = FakeLlm.respondingWith(UNPARSEABLE_RESPONSE, VALID_JSON);
        LeadExtractionService service = new LeadExtractionService(fakeLlm, validator, 3);

        ExtractionResult result = service.extractLead("raw inquiry text");

        assertThat(result.attemptsUsed()).isEqualTo(2);
        assertThat(fakeLlm.promptAt(1)).contains("could not be parsed as JSON");
    }

    @Test
    void throwsAfterExhaustingAllRetriesOnPersistentViolations() {
        FakeLlm fakeLlm = FakeLlm.respondingWith(INVALID_EMAIL_JSON, INVALID_EMAIL_JSON, INVALID_EMAIL_JSON);
        LeadExtractionService service = new LeadExtractionService(fakeLlm, validator, 3);

        assertThatThrownBy(() -> service.extractLead("raw inquiry text"))
                .isInstanceOf(SchemaValidationExhaustedException.class)
                .hasMessageContaining("3 attempt")
                .hasMessageContaining("email");

        assertThat(fakeLlm.callCount()).isEqualTo(3);
    }

    private static final class FakeLlm implements Function<String, String> {
        private final Deque<String> responses;
        private final List<String> prompts = new java.util.ArrayList<>();

        private FakeLlm(List<String> scriptedResponses) {
            this.responses = new ArrayDeque<>(scriptedResponses);
        }

        static FakeLlm respondingWith(String... responses) {
            return new FakeLlm(List.of(responses));
        }

        @Override
        public String apply(String prompt) {
            prompts.add(prompt);
            return responses.size() > 1 ? responses.poll() : responses.peek();
        }

        int callCount() {
            return prompts.size();
        }

        String promptAt(int index) {
            return prompts.get(index);
        }
    }
}
