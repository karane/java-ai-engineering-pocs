package com.pocs.springaimcpclient;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Fallback empty ToolCallbackProvider used only when spring.ai.mcp.client.enabled=false. */
@Configuration
@ConditionalOnProperty(prefix = "spring.ai.mcp.client", name = "enabled", havingValue = "false")
public class NoOpMcpToolCallbackProviderConfig {

    @Bean
    public ToolCallbackProvider noOpToolCallbackProvider() {
        return () -> new ToolCallback[0];
    }
}
