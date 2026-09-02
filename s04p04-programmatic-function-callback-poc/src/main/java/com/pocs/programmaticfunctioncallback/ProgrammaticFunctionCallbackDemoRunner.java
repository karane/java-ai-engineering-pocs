package com.pocs.programmaticfunctioncallback;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ProgrammaticFunctionCallbackDemoRunner implements CommandLineRunner {

    private final ProgrammaticFunctionCallbackService service;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    public ProgrammaticFunctionCallbackDemoRunner(ProgrammaticFunctionCallbackService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        System.out.println("[s04p04] Programmatic function-callback tool registration demo");

        if (!StringUtils.hasText(anthropicApiKey)) {
            System.out.println("[s04p04] ANTHROPIC_API_KEY not set - skipping live demo calls.");
            return;
        }

        try {
            String weatherOnly = service.askWithWeatherToolOnly("What's the weather in Lisbon?");
            System.out.println("[s04p04] Weather-tool-only request -> " + weatherOnly);
        } catch (RuntimeException e) {
            System.out.println("[s04p04] Weather-tool-only request failed: " + e.getMessage());
        }

        try {
            String stockOnly = service.askWithStockToolOnly("What's the current share price of ACME?");
            System.out.println("[s04p04] Stock-tool-only request -> " + stockOnly);
        } catch (RuntimeException e) {
            System.out.println("[s04p04] Stock-tool-only request failed: " + e.getMessage());
        }

        try {
            String dynamicBoth = service.askWithDynamicToolSelection(
                    "Give me the weather in Berlin and the share price of GLOBEX.", true, true);
            System.out.println("[s04p04] Dynamic tool selection (both tools) -> " + dynamicBoth);
        } catch (RuntimeException e) {
            System.out.println("[s04p04] Dynamic tool selection (both tools) failed: " + e.getMessage());
        }

        try {
            String packingAdvice = service.recommendPackingForCity("Tokyo");
            System.out.println("[s04p04] Multi-step reasoning (weather -> packing advice) -> " + packingAdvice);
        } catch (RuntimeException e) {
            System.out.println("[s04p04] Multi-step reasoning call failed: " + e.getMessage());
        }
    }
}
