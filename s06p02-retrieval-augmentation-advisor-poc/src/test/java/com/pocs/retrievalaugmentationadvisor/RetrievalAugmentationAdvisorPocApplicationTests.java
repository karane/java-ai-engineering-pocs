package com.pocs.retrievalaugmentationadvisor;

import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RetrievalAugmentationAdvisorPocApplicationTests {

    @Autowired
    private RetrievalAugmentationService retrievalAugmentationService;

    @Autowired
    private RetrievalAugmentationAdvisor strictAugmentationAdvisor;

    @Autowired
    private RetrievalAugmentationAdvisor permissiveAugmentationAdvisor;

    @Autowired
    private RetrievalAugmentationAdvisor multiSourceAugmentationAdvisor;

    @Test
    void contextLoadsAndWiresAllThreeAdvisors() {
        assertThat(retrievalAugmentationService).isNotNull();
        assertThat(strictAugmentationAdvisor).isNotNull();
        assertThat(permissiveAugmentationAdvisor).isNotNull();
        assertThat(multiSourceAugmentationAdvisor).isNotNull();
    }
}
