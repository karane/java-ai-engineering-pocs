package com.pocs.documenttransformer;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DocumentTransformerPocApplicationTests {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private DocumentPipelineService documentPipelineService;

    @Test
    void contextLoadsAndWiresThePipeline() {
        assertThat(vectorStore).isNotNull();
        assertThat(documentPipelineService).isNotNull();
    }
}
