package com.pocs.multiservermcpaggregation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MultiServerMcpAggregationApplicationTests {

    @Test
    void contextLoads() {
        // spring.ai.mcp.client.enabled=false in test application.yml avoids spawning either
        // subprocess server; NoOpAggregatedToolCallbackProviderConfig fills the gap so the
        // aggregator-profile beans still wire up with zero keys.
    }
}
