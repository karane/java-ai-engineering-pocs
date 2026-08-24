package com.pocs.schemavalidationretry;

import jakarta.validation.ConstraintViolation;

import java.util.Set;
import java.util.stream.Collectors;

public class SchemaValidationExhaustedException extends RuntimeException {

    private final int attemptsMade;
    private final Set<ConstraintViolation<CustomerLead>> lastViolations;

    public SchemaValidationExhaustedException(int attemptsMade, Set<ConstraintViolation<CustomerLead>> lastViolations) {
        super("Gave up after " + attemptsMade + " attempt(s); last violations: " + describe(lastViolations));
        this.attemptsMade = attemptsMade;
        this.lastViolations = lastViolations;
    }

    public int attemptsMade() {
        return attemptsMade;
    }

    public Set<ConstraintViolation<CustomerLead>> lastViolations() {
        return lastViolations;
    }

    private static String describe(Set<ConstraintViolation<CustomerLead>> violations) {
        if (violations == null || violations.isEmpty()) {
            return "(none recorded - last response was unparseable JSON)";
        }
        return violations.stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining("; "));
    }
}
