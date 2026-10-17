package com.pocs.claudedesktopmcpintegration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ClaudeDesktopMcpIntegrationApplicationTests {

    @Test
    void contextLoads() {
        // Default profile: stdio MCP server shape (web-application-type none, mcp.server.stdio=true,
        // mcp.client.enabled=false) - the exact context Claude Desktop would spawn. Proves it wires
        // up with zero keys and without needing any external process.
    }
}
