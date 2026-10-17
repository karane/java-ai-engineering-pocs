package com.pocs.claudedesktopmcpintegration;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Registers UnitConversionTools as the MCP server's ToolCallbackProvider, scoped to "!latency". */
@Configuration
@Profile("!latency")
public class ToolCallbackProviderConfig {

    @Bean
    public ToolCallbackProvider unitConversionToolCallbackProvider(UnitConversionTools unitConversionTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(unitConversionTools)
                .build();
    }
}
