package com.pocs.resourcepromptcatalog;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CatalogDemoRunner implements CommandLineRunner {

    @Autowired
    private PromptTemplateRegistry registry;

    @Autowired
    private CatalogChatService catalogChatService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String apiKey;

    @Override
    public void run(String... args) {
        System.out.println("[s02p05] Prompt catalog keys: " + registry.keys());

        String classifyPrompt = registry.render("classify", Map.of(
                "categories", "billing, technical, feedback",
                "text", "I was charged twice for my subscription this month."
        ));
        System.out.println("[s02p05] Rendered 'classify' template (no API call needed):\n" + classifyPrompt);

        if (apiKey.isBlank()) {
            System.out.println("[s02p05] ANTHROPIC_API_KEY not set — skipping live demo calls. "
                    + "Export ANTHROPIC_API_KEY and re-run to see live lookups and the v1/v2 A/B comparison.");
            return;
        }

        try {
            String classification = catalogChatService.ask("classify", Map.of(
                    "categories", "billing, technical, feedback",
                    "text", "I was charged twice for my subscription this month."
            ));
            System.out.println("[s02p05] Claude classification: " + classification);

            Map<String, String> comparison = catalogChatService.compareVersions("summarize", Map.of(
                    "text", "Spring AI provides a portable API for interacting with generative AI "
                            + "models across providers, offering abstractions like ChatClient, PromptTemplate "
                            + "and structured output converters so application code doesn't need to be "
                            + "rewritten per vendor."
            ));
            comparison.forEach((key, response) ->
                    System.out.println("[s02p05] " + key + " -> " + response));
        } catch (RuntimeException e) {
            System.out.println("[s02p05] Demo call to Claude failed, but the app started fine: " + e.getMessage());
        }
    }
}
