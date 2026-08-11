package com.pocs.springaigemini;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GeminiDemoRunner implements CommandLineRunner {

    @Autowired
    private GeminiService geminiService;

    @Override
    public void run(String... args) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("[s01p02] GEMINI_API_KEY not set — skipping live demo call. "
                    + "Export GEMINI_API_KEY and re-run to see a real Gemini response.");
            return;
        }
        try {
            String answer = geminiService.ask("In one short sentence, what is Spring AI?");
            System.out.println("[s01p02] Gemini says: " + answer);
        } catch (RuntimeException e) {
            System.out.println("[s01p02] Demo call to Gemini failed, but the app started fine: " + e.getMessage());
        }
    }
}
