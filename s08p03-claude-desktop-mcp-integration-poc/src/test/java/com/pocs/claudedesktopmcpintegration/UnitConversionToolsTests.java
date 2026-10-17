package com.pocs.claudedesktopmcpintegration;

import org.junit.jupiter.api.Test;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;

import static org.assertj.core.api.Assertions.assertThat;

class UnitConversionToolsTests {

    @Test
    void celsiusToFahrenheitConvertsCorrectly() {
        assertThat(new UnitConversionTools().celsiusToFahrenheit(100)).isEqualTo(212.0);
    }

    @Test
    void toolsExposeThreeAnnotatedMethods() {
        ToolCallback[] callbacks = ToolCallbacks.from(new UnitConversionTools());

        assertThat(callbacks)
                .extracting(callback -> callback.getToolDefinition().name())
                .containsExactlyInAnyOrder("celsiusToFahrenheit", "kilometersToMiles", "kilogramsToPounds");
    }
}
