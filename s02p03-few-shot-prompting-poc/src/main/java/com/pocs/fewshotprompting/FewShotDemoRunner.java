package com.pocs.fewshotprompting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FewShotDemoRunner implements CommandLineRunner {

    @Autowired
    private FewShotService fewShotService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    @Override
    public void run(String... args) {
        String query = "The mobile app keeps freezing whenever I open the settings screen.";
        List<ClassificationExample> examples = ClassificationExample.catalog();

        if (anthropicApiKey.isBlank()) {
            System.out.println("[s02p03] ANTHROPIC_API_KEY not set — skipping Claude demo calls.");
        } else {
            try {
                String zeroShot = fewShotService.classifyZeroShotAnthropic(query);
                System.out.println("[s02p03] Claude zero-shot: " + zeroShot);

                String fewShot = fewShotService.classifyFewShotAnthropic(query, examples);
                System.out.println("[s02p03] Claude few-shot: " + fewShot);
            } catch (RuntimeException e) {
                System.out.println("[s02p03] Claude demo call failed, but the app started fine: " + e.getMessage());
            }
        }

        if (geminiApiKey.isBlank()) {
            System.out.println("[s02p03] GEMINI_API_KEY not set — skipping Gemini demo calls.");
        } else {
            try {
                String zeroShot = fewShotService.classifyZeroShotGemini(query);
                System.out.println("[s02p03] Gemini zero-shot: " + zeroShot);

                String fewShot = fewShotService.classifyFewShotGemini(query, examples);
                System.out.println("[s02p03] Gemini few-shot: " + fewShot);
            } catch (RuntimeException e) {
                System.out.println("[s02p03] Gemini demo call failed, but the app started fine: " + e.getMessage());
            }
        }

        if (!anthropicApiKey.isBlank() && !geminiApiKey.isBlank()) {
            try {
                FewShotService.ClassificationComparison comparison = fewShotService.compare(query, examples);
                System.out.println("[s02p03] Comparison: " + comparison);
            } catch (RuntimeException e) {
                System.out.println("[s02p03] Comparison call failed, but the app started fine: " + e.getMessage());
            }
        }
    }
}
