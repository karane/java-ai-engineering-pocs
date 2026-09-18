package com.pocs.ragevaluation;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the tiny, fixed knowledge base this POC evaluates against, plus the {@link VectorStore} it lives in. */
@Configuration
public class KnowledgeBaseConfig {

    /**
     * A handful of short, self-contained facts. Each golden question in
     * {@link GoldenDataset} is answerable from exactly one of these.
     */
    static final List<Document> KNOWLEDGE_BASE = List.of(
            Document.builder()
                    .text("The Eiffel Tower was completed in 1889 for the World's Fair in Paris.")
                    .metadata("topic", "landmarks")
                    .build(),
            Document.builder()
                    .text("Spring AI is a Spring-idiomatic framework for building AI applications, "
                            + "providing portable abstractions across multiple model providers.")
                    .metadata("topic", "spring-ai")
                    .build(),
            Document.builder()
                    .text("Mount Kilimanjaro, located in Tanzania, is the tallest mountain in Africa "
                            + "at 5,895 meters.")
                    .metadata("topic", "geography")
                    .build(),
            Document.builder()
                    .text("The mitochondrion is the organelle responsible for producing most of a "
                            + "cell's ATP through cellular respiration.")
                    .metadata("topic", "biology")
                    .build());

    @Bean
    public EmbeddingModel embeddingModel() {
        return new DeterministicFakeEmbeddingModel();
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        VectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        vectorStore.add(KNOWLEDGE_BASE);
        return vectorStore;
    }
}
