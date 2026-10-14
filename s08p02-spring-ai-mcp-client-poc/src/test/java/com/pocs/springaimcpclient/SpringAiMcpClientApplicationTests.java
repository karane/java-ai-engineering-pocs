package com.pocs.springaimcpclient;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringAiMcpClientApplicationTests {

    @Test
    void contextLoads() {
        // spring.ai.mcp.client.enabled=false in test application.yml disables the MCP client
        // auto-configuration so no subprocess is spawned here - NoOpMcpToolCallbackProviderConfig
        // supplies an empty ToolCallbackProvider so the context still wires up with zero keys.
    }
}
