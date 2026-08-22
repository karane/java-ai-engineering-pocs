package com.pocs.chatcliententity;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * The domain record used to demonstrate {@code ChatClient.entity()} structured
 * output. Each field carries a {@link JsonPropertyDescription} - Spring AI's
 * {@code BeanOutputConverter} (used internally by {@code .entity()}) folds these
 * hints into the JSON schema it sends the model, steering what values land in
 * each field beyond what the field name alone implies.
 */
public record Product(

        @JsonPropertyDescription("The product's marketing name, 2-5 words, no brand prefix")
        String name,

        @JsonPropertyDescription("Retail price in US dollars, as a plain decimal number, e.g. 29.99")
        double price,

        @JsonPropertyDescription("One of: Electronics, Kitchen, Outdoor, Apparel, Home, Toys")
        String category,

        @JsonPropertyDescription("A single enticing sentence describing the product for a store listing")
        String description
) {
}
