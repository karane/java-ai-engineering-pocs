package com.pocs.retrievalaugmentationadvisor;

import java.util.List;

import org.springframework.ai.document.Document;

/** Two small, fixed document sets used across the demo runner and tests: {@code productDocs} (RAG concepts) and {@code supportDocs} (a partly-overlapping set, sharing one document with {@code productDocs} by id, plus one document unique to it). */
final class KnowledgeBase {

    /** Shared across both stores under the SAME id, so DocumentJoiner's dedup-by-id is exercised. */
    static final Document SHARED_DOCUMENT = Document.builder()
            .id("shared-rag-definition")
            .text("Retrieval-Augmented Generation, or RAG, grounds an LLM's answer in documents "
                    + "retrieved from a knowledge base, rather than relying only on the model's training data.")
            .metadata("topic", "rag")
            .build();

    private KnowledgeBase() {
    }

    static List<Document> productDocs() {
        return List.of(
                SHARED_DOCUMENT,
                Document.builder()
                        .text("RetrievalAugmentationAdvisor is a Spring AI ChatClient advisor implementing the full "
                                + "modular RAG pipeline: query transformation, expansion, retrieval, joining, "
                                + "post-processing, and query augmentation.")
                        .metadata("topic", "rag")
                        .build(),
                Document.builder()
                        .text("VectorStoreDocumentRetriever retrieves documents from a VectorStore that are "
                                + "semantically similar to a query, with optional similarity threshold, topK, and "
                                + "metadata filter expression.")
                        .metadata("topic", "retrieval")
                        .build());
    }

    static List<Document> supportDocs() {
        return List.of(
                SHARED_DOCUMENT,
                Document.builder()
                        .text("ContextualQueryAugmenter rewrites the user query by injecting retrieved document "
                                + "text into a prompt template, and can be configured to politely decline when no "
                                + "relevant documents were found instead of guessing.")
                        .metadata("topic", "augmentation")
                        .build());
    }
}
