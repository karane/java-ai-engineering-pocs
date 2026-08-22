package com.pocs.chatcliententity;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

/**
 * Demonstrates the two shapes of {@code ChatClient.entity()}:
 *
 * <ul>
 *     <li>{@link #extractOne(String)} - {@code .entity(MyRecord.class)}, parsing the model's
 *     JSON response straight into a single {@link Product}.</li>
 *     <li>{@link #extractMany(String)} - {@code .entity(new ParameterizedTypeReference<List<Product>>() {})},
 *     the generic-collection overload needed because {@code List<Product>.class} isn't
 *     expressible in Java - erasure would just give you {@code List.class} with no element
 *     type, so the converter would have nothing to build a schema for.</li>
 * </ul>
 *
 * Both methods never hand-write JSON-parsing code: {@code .entity()} injects a JSON
 * schema (derived from {@link Product}'s field types and {@code @JsonPropertyDescription}
 * hints) into the prompt's format instructions, then parses the model's JSON reply back
 * into the requested Java type.
 */
@Service
public class ProductExtractionService {

    private final ChatClient chatClient;

    public ProductExtractionService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /** Extracts a single {@link Product} from one free-text description. */
    public Product extractOne(String description) {
        return chatClient.prompt()
                .user(u -> u.text("""
                        Extract a single product from this description and return it as
                        structured data: {description}
                        """).param("description", description))
                .call()
                .entity(Product.class);
    }

    /**
     * Extracts a list of {@link Product}s from one LLM response - the generic-collection
     * overload of {@code .entity()}.
     */
    public List<Product> extractMany(String instruction) {
        return chatClient.prompt()
                .user(instruction)
                .call()
                .entity(new ParameterizedTypeReference<List<Product>>() {
                });
    }
}
