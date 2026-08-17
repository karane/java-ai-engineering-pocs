package com.pocs.systempromptpersona;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PersonaDemoRunner implements CommandLineRunner {

    private static final String ROLE = "a terse senior code reviewer who never sugar-coats feedback";
    private static final String TONE = "blunt but professional";

    @Autowired
    private PersonaService personaService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    @Override
    public void run(String... args) {
        String question = "Someone just pushed a 900-line PR with no tests. What do you say?";

        if (anthropicApiKey.isBlank()) {
            System.out.println("[s02p02] ANTHROPIC_API_KEY not set — skipping Claude persona demo.");
        } else {
            try {
                String answer = personaService.claudePersonaAnswer(ROLE, TONE, question);
                System.out.println("[s02p02] Claude (persona): " + answer);

                String multiTurn = personaService.claudeMultiTurn(ROLE, TONE,
                        "Review this: a function with no error handling.",
                        "No error handling. Fix it before I look at anything else.",
                        "What about the missing tests?");
                System.out.println("[s02p02] Claude (multi-turn): " + multiTurn);
            } catch (RuntimeException e) {
                System.out.println("[s02p02] Claude demo call failed, but the app started fine: " + e.getMessage());
            }
        }

        if (geminiApiKey.isBlank()) {
            System.out.println("[s02p02] GEMINI_API_KEY not set — skipping Gemini persona demo.");
        } else {
            try {
                String answer = personaService.geminiPersonaAnswer(ROLE, TONE, question);
                System.out.println("[s02p02] Gemini (persona): " + answer);

                String multiTurn = personaService.geminiMultiTurn(ROLE, TONE,
                        "Review this: a function with no error handling.",
                        "No error handling. Fix it before I look at anything else.",
                        "What about the missing tests?");
                System.out.println("[s02p02] Gemini (multi-turn): " + multiTurn);
            } catch (RuntimeException e) {
                System.out.println("[s02p02] Gemini demo call failed, but the app started fine: " + e.getMessage());
            }
        }

        if (!anthropicApiKey.isBlank() && !geminiApiKey.isBlank()) {
            try {
                PersonaService.PersonaComparison comparison = personaService.compare(ROLE, TONE, question);
                System.out.println("[s02p02] Comparison — Claude: " + comparison.claudeAnswer());
                System.out.println("[s02p02] Comparison — Gemini: " + comparison.geminiAnswer());
            } catch (RuntimeException e) {
                System.out.println("[s02p02] Comparison call failed, but the app started fine: " + e.getMessage());
            }
        } else {
            System.out.println("[s02p02] Both API keys required for the side-by-side comparison — skipping.");
        }
    }
}
