package com.pocs.multiservermcpaggregation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
@Profile("!server-a & !server-b")
public class AggregatingDemoRunner implements CommandLineRunner {

    private final AggregatedToolInspectionService aggregatedToolInspectionService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    public AggregatingDemoRunner(AggregatedToolInspectionService aggregatedToolInspectionService) {
        this.aggregatedToolInspectionService = aggregatedToolInspectionService;
    }

    @Override
    public void run(String... args) {
        System.out.println("[s08p04] Aggregating tools from two independent MCP servers (server-a: "
                + "unit-conversion, server-b: text-tools) into one ChatClient");

        Map<String, List<org.springframework.ai.tool.ToolCallback>> grouped =
                aggregatedToolInspectionService.toolsGroupedByName();
        System.out.println("[s08p04] Aggregated tool names: " + grouped.keySet());

        List<String> duplicates = aggregatedToolInspectionService.duplicateToolNames();
        if (duplicates.isEmpty()) {
            System.out.println("[s08p04] No tool-name collisions across the two servers.");
        } else {
            System.out.println("[s08p04] Tool-name collision(s) detected: " + duplicates
                    + " - see README for how Spring AI's MCP client resolves this.");
        }

        if (!StringUtils.hasText(anthropicApiKey)) {
            System.out.println("[s08p04] ANTHROPIC_API_KEY not set - skipping live aggregated tool-calling demo.");
            return;
        }

        try {
            String answer = aggregatedToolInspectionService.ask(
                    "Convert 100 degrees Celsius to Fahrenheit, then shout the word 'done' in all caps. "
                            + "Use the available tools and report both results.");
            System.out.println("[s08p04] Claude answer (via aggregated MCP tools) -> " + answer);
        } catch (RuntimeException e) {
            System.out.println("[s08p04] Aggregated tool-calling call failed: " + e.getMessage());
        }
    }
}
