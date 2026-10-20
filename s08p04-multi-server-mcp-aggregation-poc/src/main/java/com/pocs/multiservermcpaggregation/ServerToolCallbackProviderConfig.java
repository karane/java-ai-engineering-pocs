package com.pocs.multiservermcpaggregation;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Exposes exactly one tool bean per "server-*" profile, so each profile boots this same jar as a
 * distinct, single-purpose MCP server (the shape the aggregator profile spawns twice, over two
 * separate stdio connections).
 */
@Configuration
public class ServerToolCallbackProviderConfig {

    @Bean
    @Profile("server-a")
    public ToolCallbackProvider serverAToolCallbackProvider(UnitConversionTools unitConversionTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(unitConversionTools)
                .build();
    }

    @Bean
    @Profile("server-b")
    public ToolCallbackProvider serverBToolCallbackProvider(TextTools textTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(textTools)
                .build();
    }
}
