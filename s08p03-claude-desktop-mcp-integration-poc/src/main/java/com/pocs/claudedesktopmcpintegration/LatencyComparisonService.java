package com.pocs.claudedesktopmcpintegration;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** Compares latency of calling UnitConversionTools directly vs. over a real MCP round trip. */
@Service
@Profile("latency")
public class LatencyComparisonService {

    private final UnitConversionTools directTools;
    private final ToolCallbackProvider mcpToolCallbackProvider;

    public LatencyComparisonService(UnitConversionTools directTools, ToolCallbackProvider mcpToolCallbackProvider) {
        this.directTools = directTools;
        this.mcpToolCallbackProvider = mcpToolCallbackProvider;
    }

    public LatencyResult compare(int iterations) {
        // Warm up both paths once before timing, so JIT/connection warm-up doesn't skew the first sample.
        directTools.celsiusToFahrenheit(0);
        ToolCallback mcpCelsiusTool = findCelsiusTool();
        mcpCelsiusTool.call("{\"celsius\": 0}");

        long directStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            directTools.celsiusToFahrenheit(100);
        }
        long directElapsedNanos = System.nanoTime() - directStart;

        long mcpStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            mcpCelsiusTool.call("{\"celsius\": 100}");
        }
        long mcpElapsedNanos = System.nanoTime() - mcpStart;

        return new LatencyResult(
                iterations,
                directElapsedNanos / iterations,
                mcpElapsedNanos / iterations);
    }

    private ToolCallback findCelsiusTool() {
        for (ToolCallback callback : mcpToolCallbackProvider.getToolCallbacks()) {
            if (callback.getToolDefinition().name().equals("celsiusToFahrenheit")) {
                return callback;
            }
        }
        throw new IllegalStateException("celsiusToFahrenheit tool not found over MCP connection");
    }

    public record LatencyResult(int iterations, long avgDirectNanos, long avgMcpNanos) {
        public double mcpOverheadFactor() {
            return avgMcpNanos / (double) avgDirectNanos;
        }
    }
}
