package com.pocs.llmasjudgeevaluation;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the tiny, fixed knowledge base this POC's golden dataset is evaluated against. */
@Configuration
public class KnowledgeBaseConfig {

    static final List<Document> KNOWLEDGE_BASE = List.of(
            Document.builder()
                    .text("The Panama Canal connects the Atlantic and Pacific Oceans and opened in 1914.")
                    .metadata("topic", "geography")
                    .build(),
            Document.builder()
                    .text("Spring AI's ChatClient fluent API supports .call() for a single response and "
                            + ".stream() for a reactive Flux of response chunks.")
                    .metadata("topic", "spring-ai")
                    .build(),
            Document.builder()
                    .text("Photosynthesis converts light energy, water, and carbon dioxide into glucose "
                            + "and oxygen inside a plant's chloroplasts.")
                    .metadata("topic", "biology")
                    .build(),
            Document.builder()
                    .text("The Great Barrier Reef, off the coast of Queensland, Australia, is the world's "
                            + "largest coral reef system.")
                    .metadata("topic", "geography")
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
