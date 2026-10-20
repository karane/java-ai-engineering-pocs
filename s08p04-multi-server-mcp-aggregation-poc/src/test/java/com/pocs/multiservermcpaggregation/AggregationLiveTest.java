package com.pocs.multiservermcpaggregation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/** Live, end-to-end test spawning both server-a and server-b as real subprocesses. */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.ai.mcp.client.enabled=true",
        "spring.ai.mcp.client.stdio.connections.unit-conversion-server.command=java",
        "spring.ai.mcp.client.stdio.connections.unit-conversion-server.args[0]=-jar",
        "spring.ai.mcp.client.stdio.connections.unit-conversion-server.args[1]="
                + "target/s08p04-multi-server-mcp-aggregation-poc-0.1.0.jar",
        "spring.ai.mcp.client.stdio.connections.unit-conversion-server.args[2]=--spring.profiles.active=server-a",
        "spring.ai.mcp.client.stdio.connections.text-tools-server.command=java",
        "spring.ai.mcp.client.stdio.connections.text-tools-server.args[0]=-jar",
        "spring.ai.mcp.client.stdio.connections.text-tools-server.args[1]="
                + "target/s08p04-multi-server-mcp-aggregation-poc-0.1.0.jar",
        "spring.ai.mcp.client.stdio.connections.text-tools-server.args[2]=--spring.profiles.active=server-b"
})
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class AggregationLiveTest {

    @Autowired
    private AggregatedToolInspectionService aggregatedToolInspectionService;

    @Test
    void aggregatesToolsFromBothServersAndClaudeUsesBoth() {
        // Spring AI's McpToolCallbackProvider resolves the "ping" collision itself before this
        // service ever sees the list - DefaultMcpToolNamePrefixGenerator renames the second
        // occurrence to "alt_1_ping" (see README) - so duplicateToolNames() is expected to be empty
        // and both the original and renamed tool should be present instead.
        assertThat(aggregatedToolInspectionService.toolsGroupedByName().keySet())
                .contains("celsiusToFahrenheit", "shout", "wordCount", "ping", "alt_1_ping");
        assertThat(aggregatedToolInspectionService.duplicateToolNames()).isEmpty();

        String answer = aggregatedToolInspectionService.ask(
                "Convert 100 degrees Celsius to Fahrenheit, then shout the word done in all caps. "
                        + "Use the available tools and report both results in your final answer.");

        assertThat(answer).contains("212");
        assertThat(answer.toUpperCase()).contains("DONE");
    }
}
