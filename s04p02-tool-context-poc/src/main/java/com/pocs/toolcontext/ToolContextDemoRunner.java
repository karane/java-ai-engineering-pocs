package com.pocs.toolcontext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ToolContextDemoRunner implements CommandLineRunner {

    private static final String QUESTION = "What tier is my account, and what currency should I quote "
            + "pricing in for the 'Orbital Widget'?";

    private final ToolContextService toolContextService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    public ToolContextDemoRunner(ToolContextService toolContextService) {
        this.toolContextService = toolContextService;
    }

    @Override
    public void run(String... args) {
        System.out.println("[s04p02] ToolContext / @ToolParam / ToolCallingManager demo");

        if (!StringUtils.hasText(anthropicApiKey)) {
            System.out.println("[s04p02] ANTHROPIC_API_KEY not set - skipping live demo.");
            return;
        }

        try {
            String automaticAnswer = toolContextService.automaticToolLoop("user-42", QUESTION);
            System.out.println("[s04p02] Automatic tool loop (ChatClient) -> " + automaticAnswer);
        } catch (RuntimeException e) {
            System.out.println("[s04p02] Automatic tool loop call failed: " + e.getMessage());
        }

        try {
            String manualAnswer = toolContextService.manualToolLoop("user-7", QUESTION);
            System.out.println("[s04p02] Manual tool loop (ToolCallingManager) -> " + manualAnswer);
        } catch (RuntimeException e) {
            System.out.println("[s04p02] Manual tool loop call failed: " + e.getMessage());
        }
    }
}
