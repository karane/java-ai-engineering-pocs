package com.pocs.springaimcpserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringAiMcpServerApplicationTests {

    @LocalServerPort
    private int port;

    @Test
    void contextLoads() {
        // Boots the full webmvc MCP server (random port avoids clashing with the 8081 default) and
        // proves the ApplicationContext - including the MCP server auto-configuration and the
        // ToolCallbackProvider bean - wires up with zero API keys configured.
    }
}
