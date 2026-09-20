package com.pocs.contextualcompression;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires an in-memory {@link SimpleVectorStore} from whichever {@link EmbeddingModel} bean is on the classpath (Gemini, here - see application.yml), same as {@code s05p03}/{@code s06p01}. */
@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
