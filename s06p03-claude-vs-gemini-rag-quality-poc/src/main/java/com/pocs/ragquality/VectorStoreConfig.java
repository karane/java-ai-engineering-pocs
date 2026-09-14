package com.pocs.ragquality;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the in-memory {@link SimpleVectorStore} used by BOTH providers, pre-loaded with the fixed {@link KnowledgeBaseConfig#knowledgeBaseDocuments()} at startup. */
@Configuration
public class VectorStoreConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        return new DeterministicFakeEmbeddingModel();
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel, List<Document> knowledgeBaseDocuments) {
        VectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        vectorStore.add(knowledgeBaseDocuments);
        return vectorStore;
    }
}
