package com.pocs.resourcepromptcatalog;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Plain unit tests, no Spring context and no API key needed: they only exercise
 * classpath resource loading + StringTemplate rendering, proving the catalog
 * works before any live-call test runs.
 */
class PromptTemplateRegistryTests {

    private final PromptTemplateRegistry registry = new PromptTemplateRegistry();

    @Test
    void loadsAndRendersTemplateByKey() {
        String rendered = registry.render("classify", Map.of(
                "categories", "billing, technical",
                "text", "My invoice looks wrong."
        ));

        assertThat(rendered).contains("billing, technical");
        assertThat(rendered).contains("My invoice looks wrong.");
    }

    @Test
    void cachesTemplateInstanceAcrossLookups() {
        var first = registry.get("translate");
        var second = registry.get("translate");

        assertThat(first).isSameAs(second);
    }

    @Test
    void unknownKeyThrows() {
        assertThatThrownBy(() -> registry.get("does-not-exist"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does-not-exist");
    }

    @Test
    void rendersBothVersionsForAbComparison() {
        Map<String, String> rendered = registry.renderVersionsForComparison("summarize", Map.of(
                "text", "Spring AI is a portable API for generative AI models."
        ));

        assertThat(rendered).containsKeys("summarize:v1", "summarize:v2");
        assertThat(rendered.get("summarize:v1")).contains("Spring AI is a portable API for generative AI models.");
        assertThat(rendered.get("summarize:v2")).contains("Spring AI is a portable API for generative AI models.");
        assertThat(rendered.get("summarize:v1")).isNotEqualTo(rendered.get("summarize:v2"));
    }

    @Test
    void allCatalogKeysAreLoadable() {
        for (String key : registry.keys()) {
            assertThat(registry.get(key)).isNotNull();
        }
    }
}
