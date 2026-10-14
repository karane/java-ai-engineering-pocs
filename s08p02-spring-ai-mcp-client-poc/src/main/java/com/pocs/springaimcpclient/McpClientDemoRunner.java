package com.pocs.springaimcpclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Component
public class McpClientDemoRunner implements CommandLineRunner {

    private final McpToolCallingService mcpToolCallingService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    public McpClientDemoRunner(McpToolCallingService mcpToolCallingService) {
        this.mcpToolCallingService = mcpToolCallingService;
    }

    @Override
    public void run(String... args) {
        System.out.println("[s08p02] MCP client demo - tools discovered from the s08p01 server over stdio");
        System.out.println("[s08p02] Discovered MCP tools: "
                + Arrays.toString(mcpToolCallingService.discoveredToolNames()));

        if (!StringUtils.hasText(anthropicApiKey)) {
            System.out.println("[s08p02] ANTHROPIC_API_KEY not set - skipping live Claude + MCP-tool call.");
            return;
        }

        try {
            String answer = mcpToolCallingService.ask(
                    "Convert 100 degrees Celsius to Fahrenheit and 10 kilometers to miles. "
                            + "Use the available tools and report both results.");
            System.out.println("[s08p02] Claude answer (via MCP tools) -> " + answer);
        } catch (RuntimeException e) {
            System.out.println("[s08p02] MCP tool-calling call failed: " + e.getMessage());
        }
    }
}
