package com.pocs.springaitoolmethod;

import org.junit.jupiter.api.Test;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ToolRegistrationTests {

    @Test
    void calculatorToolsExposeThreeAnnotatedTools() {
        ToolCallback[] callbacks = ToolCallbacks.from(new CalculatorTools());

        assertThat(callbacks).hasSize(3);
        assertThat(callbacks)
                .extracting(callback -> callback.getToolDefinition().name())
                .containsExactlyInAnyOrder("add", "multiply", "celsiusToFahrenheit");
    }

    @Test
    void addToolDefinitionCarriesDescriptionAndSchema() {
        ToolCallback[] callbacks = ToolCallbacks.from(new CalculatorTools());
        ToolCallback addCallback = findByName(callbacks, "add");

        ToolDefinition definition = addCallback.getToolDefinition();
        assertThat(definition.description()).contains("Add two numbers");
        assertThat(definition.inputSchema()).contains("\"a\"").contains("\"b\"");
    }

    @Test
    void addToolCanBeInvokedDirectlyWithJsonArguments() {
        ToolCallback[] callbacks = ToolCallbacks.from(new CalculatorTools());
        ToolCallback addCallback = findByName(callbacks, "add");

        String result = addCallback.call("{\"a\": 2.5, \"b\": 4.0}");

        assertThat(result).contains("6.5");
    }

    @Test
    void timeToolCallbackIsBuiltProgrammaticallyViaFunctionToolCallbackBuilder() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-09-01T10:15:30Z"), ZoneOffset.UTC);
        TimeTools timeTools = new TimeTools(fixedClock);

        ToolCallback callback = timeTools.currentDateTimeToolCallback();

        assertThat(callback.getToolDefinition().name()).isEqualTo("get_current_date_time");
        assertThat(callback.getToolDefinition().description()).contains("current local date and time");
        assertThat(callback.call("{}")).contains("2026-09-01");
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
