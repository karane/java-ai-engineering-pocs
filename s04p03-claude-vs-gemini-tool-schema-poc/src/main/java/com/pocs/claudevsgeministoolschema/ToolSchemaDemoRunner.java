package com.pocs.claudevsgeministoolschema;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ToolSchemaDemoRunner implements CommandLineRunner {

    private static final String PARALLEL_QUESTION =
            "What's the weather like in Paris and in Tokyo right now?";
    private static final String SINGLE_QUESTION = "Tell me a fun fact about the ocean.";

    private final ToolSchemaService toolSchemaService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    public ToolSchemaDemoRunner(ToolSchemaService toolSchemaService) {
        this.toolSchemaService = toolSchemaService;
    }

    @Override
    public void run(String... args) {
        System.out.println("[s04p03] Claude vs. Gemini tool schema comparison demo");

        try {
            System.out.println("[s04p03] --- Anthropic AnthropicApi.Tool (from the shared get_weather @Tool method) ---");
            System.out.println(toolSchemaService.anthropicToolSchemaJson());
        } catch (Exception e) {
            System.out.println("[s04p03] Failed to render Anthropic tool schema: " + e.getMessage());
        }

        try {
            System.out.println("[s04p03] --- Gemini FunctionDeclaration (from the same shared @Tool method) ---");
            System.out.println(toolSchemaService.geminiFunctionDeclarationJson());
        } catch (Exception e) {
            System.out.println("[s04p03] Failed to render Gemini FunctionDeclaration: " + e.getMessage());
        }

        boolean anthropicAvailable = anthropicApiKey != null && !anthropicApiKey.isBlank();
        boolean geminiAvailable = geminiApiKey != null && !geminiApiKey.isBlank()
                && !geminiApiKey.equals("demo-placeholder-not-a-real-key");

        if (!anthropicAvailable) {
            System.out.println("[s04p03] ANTHROPIC_API_KEY not set - skipping Claude live tool-call demos.");
        } else {
            runQuiet("Anthropic parallel tool-call",
                    () -> logToolCalls("Anthropic parallel", toolSchemaService.anthropicParallelToolCall(PARALLEL_QUESTION)));
            runQuiet("Anthropic forced tool-choice",
                    () -> logToolCalls("Anthropic forced tool-choice", toolSchemaService.anthropicForcedToolChoice(SINGLE_QUESTION)));
        }

        if (!geminiAvailable) {
            System.out.println("[s04p03] GEMINI_API_KEY not set - skipping Gemini live tool-call demos.");
        } else {
            runQuiet("Gemini parallel tool-call",
                    () -> logToolCalls("Gemini parallel", toolSchemaService.geminiParallelToolCall(PARALLEL_QUESTION)));
            runQuiet("Gemini restricted tool-names (no true forced tool-choice available)",
                    () -> logToolCalls("Gemini restricted-tools", toolSchemaService.geminiRestrictedToolNames(SINGLE_QUESTION)));
        }
    }

    private void logToolCalls(String label, List<AssistantMessage.ToolCall> toolCalls) {
        System.out.println("[s04p03] " + label + " -> " + toolCalls.size() + " tool call(s) issued in one turn");
        for (AssistantMessage.ToolCall call : toolCalls) {
            System.out.println("[s04p03]   " + call.name() + "(" + call.arguments() + ")");
        }
    }

    private void runQuiet(String label, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            System.out.println("[s04p03] " + label + " call failed: " + e.getMessage());
        }
    }
}
