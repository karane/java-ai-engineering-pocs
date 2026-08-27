package com.pocs.springaitoolmethod;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ToolMethodDemoRunner implements CommandLineRunner {

    private static final String MULTI_TOOL_QUESTION =
            "First, what is today's date and time? Then add 12.5 and 7.3, multiply that sum by 3, "
                    + "and finally convert 100 degrees Celsius to Fahrenheit. Report all the results.";

    private final ToolCallingService toolCallingService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    public ToolMethodDemoRunner(ToolCallingService toolCallingService) {
        this.toolCallingService = toolCallingService;
    }

    @Override
    public void run(String... args) {
        System.out.println("[s04p01] @Tool methods + ToolCallback.builder() + automatic multi-turn "
                + "tool execution loop demo");

        if (!StringUtils.hasText(anthropicApiKey)) {
            System.out.println("[s04p01] ANTHROPIC_API_KEY not set - skipping Claude tool-calling demo.");
        } else {
            try {
                String answer = toolCallingService.askAnthropic(MULTI_TOOL_QUESTION);
                System.out.println("[s04p01] Claude multi-tool answer -> " + answer);
            } catch (RuntimeException e) {
                System.out.println("[s04p01] Claude tool-calling call failed: " + e.getMessage());
            }
        }

        if (!StringUtils.hasText(geminiApiKey) || geminiApiKey.equals("demo-placeholder-not-a-real-key")) {
            System.out.println("[s04p01] GEMINI_API_KEY not set - skipping Gemini tool-calling demo.");
        } else {
            try {
                String answer = toolCallingService.askGemini(MULTI_TOOL_QUESTION);
                System.out.println("[s04p01] Gemini multi-tool answer -> " + answer);
            } catch (RuntimeException e) {
                System.out.println("[s04p01] Gemini tool-calling call failed: " + e.getMessage());
            }
        }
    }
}
