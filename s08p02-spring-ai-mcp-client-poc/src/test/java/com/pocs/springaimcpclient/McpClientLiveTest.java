package com.pocs.springaimcpclient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/** Live, end-to-end test that spawns the real s08p01 server jar as a stdio subprocess. */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.ai.mcp.client.enabled=true",
        "spring.ai.mcp.client.stdio.connections.unit-conversion-server.command=java",
        "spring.ai.mcp.client.stdio.connections.unit-conversion-server.args[0]=-jar",
        "spring.ai.mcp.client.stdio.connections.unit-conversion-server.args[1]="
                + "../s08p01-spring-ai-mcp-server-poc/target/s08p01-spring-ai-mcp-server-poc-0.1.0.jar",
        "spring.ai.mcp.client.stdio.connections.unit-conversion-server.args[2]=--spring.profiles.active=stdio"
})
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class McpClientLiveTest {

    @Autowired
    private McpToolCallingService mcpToolCallingService;

    @Test
    void claudeUsesTheMcpServersCelsiusToFahrenheitTool() {
        String[] names = mcpToolCallingService.discoveredToolNames();
        assertThat(names).contains("celsiusToFahrenheit");

        String answer = mcpToolCallingService.ask(
                "Convert 100 degrees Celsius to Fahrenheit using the available tool. "
                        + "Reply with only the final number.");

        assertThat(answer).contains("212");
    }
}
