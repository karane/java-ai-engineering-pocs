package com.pocs.beanoutputconverter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BeanOutputConverterPocApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring context (ChatClient, service, runner) wires up cleanly
        // with no ANTHROPIC_API_KEY set - the demo runner must skip gracefully rather
        // than throwing during startup.
    }
}
