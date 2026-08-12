package com.pocs.chatclientfluent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FluentChatDemoRunner implements CommandLineRunner {

    @Autowired
    private FluentChatService fluentChatService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String apiKey;

    @Override
    public void run(String... args) {
        if (apiKey.isBlank()) {
            System.out.println("[s01p03] ANTHROPIC_API_KEY not set — skipping live demo calls. "
                    + "Export ANTHROPIC_API_KEY and re-run to see real Claude responses.");
            return;
        }
        try {
            String question = "What is the capital of France?";

            String defaultAnswer = fluentChatService.askWithDefaultPersona(question);
            System.out.println("[s01p03] Default persona: " + defaultAnswer);

            String pirateAnswer = fluentChatService.askWithSystemOverride(
                    "You are a pirate. Answer in pirate speak, one sentence.", question);
            System.out.println("[s01p03] System override (pirate): " + pirateAnswer);

            String noPersonaAnswer = fluentChatService.askWithNoPersona(question);
            System.out.println("[s01p03] No persona (manual ChatClient): " + noPersonaAnswer);

            var response = fluentChatService.askForFullResponse(question);
            System.out.println("[s01p03] Full response metadata: " + fluentChatService.describeGeneration(response));
        } catch (RuntimeException e) {
            System.out.println("[s01p03] Demo call to Claude failed, but the app started fine: " + e.getMessage());
        }
    }
}
