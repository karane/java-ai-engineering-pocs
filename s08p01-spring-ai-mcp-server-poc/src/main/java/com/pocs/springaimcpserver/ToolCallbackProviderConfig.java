package com.pocs.springaimcpserver;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers {@link UnitConversionTools}' @Tool methods as a {@link ToolCallbackProvider} bean. */
@Configuration
public class ToolCallbackProviderConfig {

    @Bean
    public ToolCallbackProvider unitConversionToolCallbackProvider(UnitConversionTools unitConversionTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(unitConversionTools)
                .build();
    }
}
