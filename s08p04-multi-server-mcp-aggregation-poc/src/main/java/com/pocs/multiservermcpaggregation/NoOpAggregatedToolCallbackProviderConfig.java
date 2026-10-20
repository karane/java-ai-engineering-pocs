package com.pocs.multiservermcpaggregation;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fallback used only when spring.ai.mcp.client.enabled=false (the test application.yml) - keeps
 * contextLoads() fast and independent of both subprocesses actually being spawnable.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.ai.mcp.client", name = "enabled", havingValue = "false")
public class NoOpAggregatedToolCallbackProviderConfig {

    @Bean
    public ToolCallbackProvider noOpAggregatedToolCallbackProvider() {
        return () -> new ToolCallback[0];
    }
}
