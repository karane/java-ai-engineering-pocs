package com.pocs.ragevaluation;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RagEvaluationPocApplicationTests {

    @Autowired
    private VectorStore vectorStore;

    @Test
    void contextLoadsAndWiresVectorStore() {
        assertThat(vectorStore).isNotNull();
    }
}
