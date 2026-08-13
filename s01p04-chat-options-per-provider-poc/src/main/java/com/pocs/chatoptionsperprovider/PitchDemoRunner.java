package com.pocs.chatoptionsperprovider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PitchDemoRunner implements CommandLineRunner {

    @Autowired
    private PitchService pitchService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    @Override
    public void run(String... args) {
        String product = "a self-refilling coffee mug";

        if (anthropicApiKey.isBlank()) {
            System.out.println("[s01p04] ANTHROPIC_API_KEY not set — skipping Claude demo calls.");
        } else {
            try {
                String defaultPitch = pitchService.anthropicPitchWithDefaults(product);
                System.out.println("[s01p04] Claude (defaults): " + defaultPitch);

                String creativePitch = pitchService.anthropicPitchWithOptions(product, PitchTuning.creative());
                System.out.println("[s01p04] Claude (creative override): " + creativePitch);

                String topPPitch = pitchService.anthropicPitchWithTopP(product, PitchTuning.conservative());
                System.out.println("[s01p04] Claude (topP override): " + topPPitch);
            } catch (RuntimeException e) {
                System.out.println("[s01p04] Claude demo call failed, but the app started fine: " + e.getMessage());
            }
        }

        if (geminiApiKey.isBlank()) {
            System.out.println("[s01p04] GEMINI_API_KEY not set — skipping Gemini demo calls.");
        } else {
            try {
                String defaultPitch = pitchService.geminiPitchWithDefaults(product);
                System.out.println("[s01p04] Gemini (defaults): " + defaultPitch);

                String conservativePitch = pitchService.geminiPitchWithOptions(product, PitchTuning.conservative());
                System.out.println("[s01p04] Gemini (conservative override): " + conservativePitch);
            } catch (RuntimeException e) {
                System.out.println("[s01p04] Gemini demo call failed, but the app started fine: " + e.getMessage());
            }
        }
    }
}
