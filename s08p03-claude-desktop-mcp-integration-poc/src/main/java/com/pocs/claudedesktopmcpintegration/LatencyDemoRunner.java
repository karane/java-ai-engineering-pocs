package com.pocs.claudedesktopmcpintegration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("latency")
public class LatencyDemoRunner implements CommandLineRunner {

    private final LatencyComparisonService latencyComparisonService;

    public LatencyDemoRunner(LatencyComparisonService latencyComparisonService) {
        this.latencyComparisonService = latencyComparisonService;
    }

    @Override
    public void run(String... args) {
        LatencyComparisonService.LatencyResult result = latencyComparisonService.compare(50);

        System.err.println("[s08p03] Latency comparison over " + result.iterations() + " calls to celsiusToFahrenheit:");
        System.err.println("[s08p03]   direct @Tool call  : " + (result.avgDirectNanos() / 1000) + " us/call avg");
        System.err.println("[s08p03]   MCP tool call      : " + (result.avgMcpNanos() / 1000) + " us/call avg");
        System.err.println("[s08p03]   MCP overhead factor: " + String.format("%.1f", result.mcpOverheadFactor()) + "x");
    }
}
