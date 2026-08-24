package com.pocs.schemavalidationretry;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerLeadValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validLeadHasNoViolations() {
        CustomerLead lead = new CustomerLead(
                "Jane Doe", "jane.doe@example.com", "+1 (555) 123-4567",
                "Interested in the enterprise pricing tier for a 200-seat team.", 0.92);

        assertThat(validator.validate(lead)).isEmpty();
    }

    @Test
    void blankFullNameViolatesSize() {
        CustomerLead lead = new CustomerLead(
                "J", "jane.doe@example.com", "+1 (555) 123-4567",
                "Interested in the enterprise pricing tier.", 0.9);

        Set<ConstraintViolation<CustomerLead>> violations = validator.validate(lead);

        assertThat(violations).isNotEmpty();
        assertThat(propertyPaths(violations)).contains("fullName");
    }

    @Test
    void malformedEmailViolatesPattern() {
        CustomerLead lead = new CustomerLead(
                "Jane Doe", "not-an-email", "+1 (555) 123-4567",
                "Interested in the enterprise pricing tier.", 0.9);

        Set<ConstraintViolation<CustomerLead>> violations = validator.validate(lead);

        assertThat(propertyPaths(violations)).contains("email");
    }

    @Test
    void malformedPhoneViolatesPattern() {
        CustomerLead lead = new CustomerLead(
                "Jane Doe", "jane.doe@example.com", "call-me-maybe",
                "Interested in the enterprise pricing tier.", 0.9);

        Set<ConstraintViolation<CustomerLead>> violations = validator.validate(lead);

        assertThat(propertyPaths(violations)).contains("phone");
    }

    @Test
    void tooShortInquirySummaryViolatesSize() {
        CustomerLead lead = new CustomerLead(
                "Jane Doe", "jane.doe@example.com", "+1 (555) 123-4567", "too short", 0.9);

        Set<ConstraintViolation<CustomerLead>> violations = validator.validate(lead);

        assertThat(propertyPaths(violations)).contains("inquirySummary");
    }

    @Test
    void confidenceScoreOutOfRangeViolatesDecimalBounds() {
        CustomerLead lead = new CustomerLead(
                "Jane Doe", "jane.doe@example.com", "+1 (555) 123-4567",
                "Interested in the enterprise pricing tier.", 1.5);

        Set<ConstraintViolation<CustomerLead>> violations = validator.validate(lead);

        assertThat(propertyPaths(violations)).contains("confidenceScore");
    }

    private static Set<String> propertyPaths(Set<ConstraintViolation<CustomerLead>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
