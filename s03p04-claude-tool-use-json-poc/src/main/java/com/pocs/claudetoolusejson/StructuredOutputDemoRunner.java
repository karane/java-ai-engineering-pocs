package com.pocs.claudetoolusejson;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class StructuredOutputDemoRunner implements CommandLineRunner {

    private static final String SAMPLE_INVOICE_TEXT =
            "Acme Robotics billed us $4,250.75, due on 2026-09-15, for two line items: "
                    + "\"20x actuator units\" and \"on-site calibration service\".";

    private final StructuredOutputService structuredOutputService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    public StructuredOutputDemoRunner(StructuredOutputService structuredOutputService) {
        this.structuredOutputService = structuredOutputService;
    }

    @Override
    public void run(String... args) {
        System.out.println("[s03p04] Native structured output vs. prompt-engineered JSON demo");

        if (!StringUtils.hasText(anthropicApiKey)) {
            System.out.println("[s03p04] ANTHROPIC_API_KEY not set - skipping Anthropic forced "
                    + "tool-use and prompt-engineered-JSON-on-Claude demos.");
        } else {
            try {
                Invoice viaToolUse = structuredOutputService.anthropicForcedToolUse(SAMPLE_INVOICE_TEXT);
                System.out.println("[s03p04] Anthropic forced tool-use -> " + viaToolUse);
            } catch (RuntimeException e) {
                System.out.println("[s03p04] Anthropic forced tool-use call failed: " + e.getMessage());
            }

            try {
                Invoice viaPromptClaude = structuredOutputService.promptEngineeredJson(
                        structuredOutputService.anthropicChatClient(), SAMPLE_INVOICE_TEXT);
                System.out.println("[s03p04] Prompt-engineered JSON (Claude) -> " + viaPromptClaude);
            } catch (RuntimeException e) {
                System.out.println("[s03p04] Prompt-engineered JSON (Claude) call failed: " + e.getMessage());
            }
        }

        if (!StringUtils.hasText(geminiApiKey) || geminiApiKey.equals("demo-placeholder-not-a-real-key")) {
            System.out.println("[s03p04] GEMINI_API_KEY not set - skipping Gemini native JSON mode "
                    + "and prompt-engineered-JSON-on-Gemini demos.");
        } else {
            try {
                Invoice viaJsonMode = structuredOutputService.geminiNativeJsonMode(SAMPLE_INVOICE_TEXT);
                System.out.println("[s03p04] Gemini native JSON mode -> " + viaJsonMode);
            } catch (RuntimeException e) {
                System.out.println("[s03p04] Gemini native JSON mode call failed: " + e.getMessage());
            }

            try {
                Invoice viaPromptGemini = structuredOutputService.promptEngineeredJson(
                        structuredOutputService.geminiChatClient(), SAMPLE_INVOICE_TEXT);
                System.out.println("[s03p04] Prompt-engineered JSON (Gemini) -> " + viaPromptGemini);
            } catch (RuntimeException e) {
                System.out.println("[s03p04] Prompt-engineered JSON (Gemini) call failed: " + e.getMessage());
            }
        }
    }
}
