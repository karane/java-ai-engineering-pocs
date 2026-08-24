package com.pocs.schemavalidationretry;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerLead(

        @NotNull
        @Size(min = 2, max = 100, message = "must be between 2 and 100 characters")
        @JsonPropertyDescription("The lead's full name, e.g. 'Jane Doe'")
        String fullName,

        @NotNull
        @Pattern(
                regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$",
                message = "must look like a valid email address (user@domain.tld)")
        @JsonPropertyDescription("The lead's email address")
        String email,

        @NotNull
        @Pattern(
                regexp = "^\\+?[0-9()\\-. ]{7,20}$",
                message = "must be a phone number using only digits, spaces, and + ( ) - . characters, 7-20 chars long")
        @JsonPropertyDescription("The lead's phone number, digits/spaces/() - . and optional leading +")
        String phone,

        @NotNull
        @Size(min = 10, max = 500, message = "must be between 10 and 500 characters")
        @JsonPropertyDescription("A one-to-two sentence summary of what the lead is asking about")
        String inquirySummary,

        @DecimalMin(value = "0.0", message = "must be >= 0.0")
        @DecimalMax(value = "1.0", message = "must be <= 1.0")
        @JsonPropertyDescription("Self-reported confidence (0.0-1.0) that the extracted fields are accurate")
        double confidenceScore
) {
}
