package com.pocs.simplevectorstore;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SimpleVectorStorePocApplicationTests {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private SimpleVectorStoreService simpleVectorStoreService;

    @Test
    void contextLoadsAndWiresSimpleVectorStore() {
        assertThat(vectorStore).isNotNull();
        assertThat(simpleVectorStoreService).isNotNull();
    }
}
