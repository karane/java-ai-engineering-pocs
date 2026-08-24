package com.pocs.schemavalidationretry;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SchemaValidationRetryApplicationTests {

    @Test
    void contextLoads() {
        // Boots the full Spring context with zero API keys configured: proves ChatClientConfig,
        // the autoconfigured Validator, LeadExtractionService, and LeadExtractionDemoRunner all
        // wire up cleanly and the demo runner skips gracefully without a key.
    }
}
