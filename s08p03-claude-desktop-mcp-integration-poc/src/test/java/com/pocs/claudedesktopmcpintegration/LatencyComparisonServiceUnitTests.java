package com.pocs.claudedesktopmcpintegration;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Exercises LatencyComparisonService's comparison math with a mocked MCP ToolCallback. */
class LatencyComparisonServiceUnitTests {

    @Test
    void mcpPathIsMeasuredSlowerThanDirectPathWhenItHasExtraOverhead() {
        ToolCallback mockMcpCallback = mock(ToolCallback.class);
        ToolDefinition definition = mock(ToolDefinition.class);
        when(definition.name()).thenReturn("celsiusToFahrenheit");
        when(mockMcpCallback.getToolDefinition()).thenReturn(definition);
        when(mockMcpCallback.call("{\"celsius\": 0}")).thenReturn("32.0");
        when(mockMcpCallback.call("{\"celsius\": 100}")).thenAnswer(invocation -> {
            Thread.sleep(1);
            return "212.0";
        });

        ToolCallbackProvider provider = () -> new ToolCallback[] {mockMcpCallback};
        LatencyComparisonService service = new LatencyComparisonService(new UnitConversionTools(), provider);

        LatencyComparisonService.LatencyResult result = service.compare(5);

        assertThat(result.iterations()).isEqualTo(5);
        assertThat(result.avgMcpNanos()).isGreaterThan(result.avgDirectNanos());
        assertThat(result.mcpOverheadFactor()).isGreaterThan(1.0);
    }

    @Test
    void missingToolOverMcpFailsFast() {
        ToolCallbackProvider emptyProvider = () -> new ToolCallback[0];
        LatencyComparisonService service = new LatencyComparisonService(new UnitConversionTools(), emptyProvider);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.compare(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("celsiusToFahrenheit");
    }
}
