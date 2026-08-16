package com.pocs.prompttemplate;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PromptTemplateDemoRunner implements CommandLineRunner {

    @Autowired
    private PromptTemplateService promptTemplateService;

    @Autowired
    private ChatClient chatClient;

    @Value("${spring.ai.anthropic.api-key:}")
    private String apiKey;

    @Override
    public void run(String... args) {
        String inlineRendered = promptTemplateService.renderInlineTemplate(
                "Tell me a fun fact about {topic} in one sentence.",
                Map.of("topic", "the Roman Empire"));
        System.out.println("[s02p01] Inline template rendered: " + inlineRendered);

        String resourceRendered = promptTemplateService.renderGreetingFromResource(
                Map.of("name", "Ada", "company", "Spring AI Inc."));
        System.out.println("[s02p01] Resource-loaded template rendered: " + resourceRendered);

        if (apiKey.isBlank()) {
            System.out.println("[s02p01] ANTHROPIC_API_KEY not set — skipping live demo call. "
                    + "Export ANTHROPIC_API_KEY and re-run to see a real Claude response.");
            return;
        }
        try {
            String answer = chatClient.prompt().user(inlineRendered).call().content();
            System.out.println("[s02p01] Claude says: " + answer);
        } catch (RuntimeException e) {
            System.out.println("[s02p01] Demo call to Claude failed, but the app started fine: " + e.getMessage());
        }
    }
}
