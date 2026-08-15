package com.pocs.multimodelprofile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ChatDemoRunner implements CommandLineRunner {

    @Autowired
    private ChatService chatService;

    @Value("${app.provider}")
    private String activeProvider;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    @Override
    public void run(String... args) {
        System.out.println("[s01p06] Active provider profile: " + activeProvider);

        String question = "In one sentence, what makes you different from other AI models?";
        boolean keyMissing = ("claude".equals(activeProvider) && anthropicApiKey.isBlank())
                || ("gemini".equals(activeProvider) && geminiApiKey.isBlank());

        if (keyMissing) {
            System.out.println("[s01p06] API key for '" + activeProvider + "' not set — skipping demo call.");
            return;
        }

        try {
            String answer = chatService.ask(question);
            System.out.println("[s01p06] (" + activeProvider + ") " + answer);
        } catch (RuntimeException e) {
            System.out.println("[s01p06] Demo call failed, but the app started fine: " + e.getMessage());
        }
    }
}
