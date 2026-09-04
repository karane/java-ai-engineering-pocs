package com.pocs.embeddingmodel;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EmbeddingModelPocApplicationTests {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingService embeddingService;

    @Test
    void contextLoadsAndWiresEmbeddingModel() {
        assertThat(embeddingModel).isNotNull();
        assertThat(embeddingService).isNotNull();
    }
}
