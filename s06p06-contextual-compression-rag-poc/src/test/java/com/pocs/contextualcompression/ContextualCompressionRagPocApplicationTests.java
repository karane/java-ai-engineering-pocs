package com.pocs.contextualcompression;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Proves the whole context. */
@SpringBootTest
class ContextualCompressionRagPocApplicationTests {

    @Autowired
    private ContextualCompressionService contextualCompressionService;

    @Autowired
    private org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;

    @Test
    void contextLoads() {
        assertThat(contextualCompressionService).isNotNull();
        assertThat(retrievalAugmentationAdvisor).isNotNull();
    }
}
