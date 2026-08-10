package com.pocs.springaianthropic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ClaudeDemoRunner implements CommandLineRunner {

    @Autowired
    private ClaudeService claudeService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String apiKey;

    @Override
    public void run(String... args) {
        if (apiKey.isBlank()) {
            System.out.println("[s01p01] ANTHROPIC_API_KEY not set — skipping live demo call. "
                    + "Export ANTHROPIC_API_KEY and re-run to see a real Claude response.");
            return;
        }
        try {
            String answer = claudeService.ask("In one short sentence, what is Spring AI?");
            System.out.println("[s01p01] Claude says: " + answer);
        } catch (RuntimeException e) {
            System.out.println("[s01p01] Demo call to Claude failed, but the app started fine: " + e.getMessage());
        }
    }
}
