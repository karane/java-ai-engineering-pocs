package com.pocs.resourcepromptcatalog;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class CatalogChatServiceLiveTest {

    @Autowired
    private CatalogChatService catalogChatService;

    @Test
    void askRendersTemplateAndCallsClaude() {
        String response = catalogChatService.ask("translate", Map.of(
                "targetLanguage", "French",
                "text", "Good morning"
        ));

        assertThat(response).isNotBlank();
    }

    @Test
    void compareVersionsRunsBothPromptVersionsAgainstClaude() {
        Map<String, String> responses = catalogChatService.compareVersions("summarize", Map.of(
                "text", "Spring AI provides a portable API for interacting with generative AI "
                        + "models across providers, offering abstractions like ChatClient, PromptTemplate "
                        + "and structured output converters so application code doesn't need to be "
                        + "rewritten per vendor."
        ));

        assertThat(responses).containsKeys("summarize:v1", "summarize:v2");
        assertThat(responses.get("summarize:v1")).isNotBlank();
        assertThat(responses.get("summarize:v2")).isNotBlank();
    }
}
