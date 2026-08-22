package com.pocs.chatcliententity;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Un-gated unit test proving the Product record's @JsonPropertyDescription
 * wiring - the field-level hints that steer schema generation for
 * ChatClient.entity() - without needing a live API call.
 */
class ProductTests {

    @Test
    void everyRecordComponentCarriesAJsonPropertyDescription() throws NoSuchFieldException {
        // @JsonPropertyDescription targets FIELD/METHOD, not RECORD_COMPONENT, so the
        // JDK does not mirror it onto RecordComponent.getAnnotation() - read the
        // backing field instead, which is what Jackson's schema generator inspects.
        for (RecordComponent component : Product.class.getRecordComponents()) {
            Field backingField = Product.class.getDeclaredField(component.getName());
            JsonPropertyDescription annotation = backingField.getAnnotation(JsonPropertyDescription.class);
            assertThat(annotation)
                    .as("record component '%s' should carry @JsonPropertyDescription", component.getName())
                    .isNotNull();
            assertThat(annotation.value()).isNotBlank();
        }
    }

    @Test
    void recordConstructsAndExposesAllFields() {
        Product product = new Product("Trail Bottle", 24.99, "Outdoor", "Keeps drinks cold for 24 hours.");

        assertThat(product.name()).isEqualTo("Trail Bottle");
        assertThat(product.price()).isEqualTo(24.99);
        assertThat(product.category()).isEqualTo("Outdoor");
        assertThat(product.description()).isEqualTo("Keeps drinks cold for 24 hours.");
    }
}
