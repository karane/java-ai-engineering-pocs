package com.pocs.schemavalidationretry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LeadExtractionDemoRunner implements CommandLineRunner {

    @Autowired
    private LeadExtractionService leadExtractionService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String apiKey;

    @Override
    public void run(String... args) {
        if (apiKey.isBlank()) {
            System.out.println("[s03p05] ANTHROPIC_API_KEY not set — skipping live demo call. "
                    + "Export ANTHROPIC_API_KEY and re-run to see the validated extraction + retry loop live.");
            return;
        }

        String rawInquiry = "Hi, I'm Jane Doe, you can reach me at jane.doe@example.com or "
                + "+1 (555) 123-4567. We're a 200-seat team evaluating your enterprise pricing tier "
                + "and would like a callback this week.";

        try {
            ExtractionResult result = leadExtractionService.extractLead(rawInquiry);
            System.out.println("[s03p05] Extracted a valid lead after " + result.attemptsUsed()
                    + " attempt(s): " + result.lead());
        } catch (SchemaValidationExhaustedException e) {
            System.out.println("[s03p05] Extraction exhausted its retries: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("[s03p05] Demo call to Claude failed, but the app started fine: " + e.getMessage());
        }
    }
}
