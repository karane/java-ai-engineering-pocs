package com.pocs.mapoutputconverter;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ExtractionDemoRunner implements CommandLineRunner {

    private static final String SAMPLE_TEXT =
            "Order #4521 was placed by Priya Nair on 2026-01-14 for 3 units of the "
                    + "Trailrunner 2.0 hiking boot, shipped to Bengaluru, total $214.50.";

    private final ChatClient anthropicChatClient;
    private final ChatClient geminiChatClient;
    private final ExtractionService extractionService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    public ExtractionDemoRunner(ChatClient anthropicChatClient, ChatClient geminiChatClient,
            ExtractionService extractionService) {
        this.anthropicChatClient = anthropicChatClient;
        this.geminiChatClient = geminiChatClient;
        this.extractionService = extractionService;
    }

    @Override
    public void run(String... args) {
        if (anthropicApiKey.isBlank()) {
            System.out.println("[s03p03] ANTHROPIC_API_KEY not set — skipping Claude demo calls.");
        } else {
            try {
                Map<String, Object> fields = extractionService.extractKeyValuePairs(anthropicChatClient, SAMPLE_TEXT);
                System.out.println("[s03p03] Claude MapOutputConverter: " + fields);

                List<String> items = extractionService.extractList(anthropicChatClient, "hiking boot brands");
                System.out.println("[s03p03] Claude ListOutputConverter: " + items);
            } catch (RuntimeException e) {
                System.out.println("[s03p03] Claude demo call failed, but the app started fine: " + e.getMessage());
            }
        }

        if (geminiApiKey.isBlank()) {
            System.out.println("[s03p03] GEMINI_API_KEY not set — skipping Gemini demo calls.");
        } else {
            try {
                Map<String, Object> fields = extractionService.extractKeyValuePairs(geminiChatClient, SAMPLE_TEXT);
                System.out.println("[s03p03] Gemini MapOutputConverter: " + fields);

                List<String> items = extractionService.extractList(geminiChatClient, "hiking boot brands");
                System.out.println("[s03p03] Gemini ListOutputConverter: " + items);
            } catch (RuntimeException e) {
                System.out.println("[s03p03] Gemini demo call failed, but the app started fine: " + e.getMessage());
            }
        }
    }
}
