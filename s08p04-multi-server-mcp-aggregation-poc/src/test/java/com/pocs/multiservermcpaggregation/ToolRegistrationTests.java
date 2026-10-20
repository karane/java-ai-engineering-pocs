package com.pocs.multiservermcpaggregation;

import org.junit.jupiter.api.Test;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;

import static org.assertj.core.api.Assertions.assertThat;

class ToolRegistrationTests {

    @Test
    void serverAExposesUnitConversionTools() {
        ToolCallback[] callbacks = ToolCallbacks.from(new UnitConversionTools());

        assertThat(callbacks)
                .extracting(c -> c.getToolDefinition().name())
                .containsExactlyInAnyOrder("celsiusToFahrenheit", "ping");
    }

    @Test
    void serverBExposesTextTools() {
        ToolCallback[] callbacks = ToolCallbacks.from(new TextTools());

        assertThat(callbacks)
                .extracting(c -> c.getToolDefinition().name())
                .containsExactlyInAnyOrder("shout", "wordCount", "ping");
    }

    @Test
    void bothServersDefineAToolNamedPingOnPurpose() {
        ToolCallback serverAPing = findByName(ToolCallbacks.from(new UnitConversionTools()), "ping");
        ToolCallback serverBPing = findByName(ToolCallbacks.from(new TextTools()), "ping");

        assertThat(serverAPing.call("{}")).contains("server-a");
        assertThat(serverBPing.call("{}")).contains("server-b");
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
