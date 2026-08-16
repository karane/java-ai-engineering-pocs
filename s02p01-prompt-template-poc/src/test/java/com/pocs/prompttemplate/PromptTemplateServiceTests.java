package com.pocs.prompttemplate;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class PromptTemplateServiceTests {

    private final PromptTemplateService service = new PromptTemplateService(new DefaultResourceLoader());

    @Test
    void rendersInlineTemplateWithSingleVariable() {
        String rendered = service.renderInlineTemplate(
                "Tell me a fun fact about {topic}.", Map.of("topic", "octopuses"));

        assertThat(rendered).isEqualTo("Tell me a fun fact about octopuses.");
    }

    @Test
    void rendersInlineTemplateWithMultipleVariables() {
        String rendered = service.renderInlineTemplate(
                "{greeting}, {name}!", Map.of("greeting", "Hello", "name", "World"));

        assertThat(rendered).isEqualTo("Hello, World!");
    }

    @Test
    void rendersGreetingTemplateLoadedFromClasspathResource() {
        String rendered = service.renderGreetingFromResource(
                Map.of("name", "Ada", "company", "Spring AI Inc."));

        assertThat(rendered).contains("Ada");
        assertThat(rendered).contains("Spring AI Inc.");
    }

    @Test
    void resourceTemplateIsCachedAndReusedAcrossRenders() {
        String first = service.renderGreetingFromResource(Map.of("name", "Ada", "company", "Acme"));
        String second = service.renderGreetingFromResource(Map.of("name", "Grace", "company", "Acme"));

        assertThat(first).contains("Ada");
        assertThat(second).contains("Grace");
        assertThat(first).isNotEqualTo(second);
    }
}
