package com.pocs.springaimcpserver;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;

import static org.assertj.core.api.Assertions.assertThat;

class ToolRegistrationTests {

    @Test
    void unitConversionToolsExposeThreeAnnotatedTools() {
        ToolCallbackProvider provider = MethodToolCallbackProvider.builder()
                .toolObjects(new UnitConversionTools())
                .build();

        ToolCallback[] callbacks = provider.getToolCallbacks();

        assertThat(callbacks).hasSize(3);
        assertThat(callbacks)
                .extracting(callback -> callback.getToolDefinition().name())
                .containsExactlyInAnyOrder("celsiusToFahrenheit", "kilometersToMiles", "kilogramsToPounds");
    }

    @Test
    void celsiusToFahrenheitToolCanBeInvokedDirectly() {
        ToolCallbackProvider provider = MethodToolCallbackProvider.builder()
                .toolObjects(new UnitConversionTools())
                .build();
        ToolCallback callback = findByName(provider.getToolCallbacks(), "celsiusToFahrenheit");

        String result = callback.call("{\"celsius\": 100}");

        assertThat(result).contains("212");
    }

    private ToolCallback findByName(ToolCallback[] callbacks, String name) {
        for (ToolCallback callback : callbacks) {
            if (callback.getToolDefinition().name().equals(name)) {
                return callback;
            }
        }
        throw new IllegalStateException("No tool named " + name);
    }
}
