package com.pocs.claudetoolusejson;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

@JsonClassDescription("An invoice extracted from a natural-language description, with vendor, amount, "
        + "due date, and line items")
public record Invoice(
        @JsonPropertyDescription("The vendor or company name that issued the invoice")
        String vendor,

        @JsonPropertyDescription("The total amount due, in US dollars, as a decimal number")
        Double totalAmount,

        @JsonPropertyDescription("The due date in ISO-8601 format (yyyy-MM-dd)")
        String dueDate,

        @JsonPropertyDescription("Short line-item descriptions billed on this invoice")
        List<String> lineItems
) {
}
