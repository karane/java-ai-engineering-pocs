package com.pocs.multimodelprofile;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("claude")
class MultiModelProfileApplicationTests {

    @Test
    void contextLoads() {
    }
}
