package com.pocs.redisvectorstore;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisVectorStorePocApplicationTests {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Test
    void contextLoadsAndWiresEmbeddingModelWithNoRedisRequired() {
        assertThat(embeddingModel).isNotNull();
    }
}
