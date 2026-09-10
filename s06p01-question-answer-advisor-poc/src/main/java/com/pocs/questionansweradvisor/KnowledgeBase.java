package com.pocs.questionansweradvisor;

import java.util.List;

import org.springframework.ai.document.Document;

final class KnowledgeBase {

    private KnowledgeBase() {
    }

    static List<Document> sampleDocuments() {
        return List.of(
                Document.builder()
                        .text("Spring AI is a Java framework that provides a portable API across LLM providers "
                                + "like Anthropic, OpenAI, and Google Gemini, for building AI-powered applications.")
                        .metadata("topic", "spring-ai")
                        .build(),
                Document.builder()
                        .text("ChatClient is Spring AI's fluent, builder-style API for sending prompts to a "
                                + "chat model and reading back structured or plain-text responses.")
                        .metadata("topic", "spring-ai")
                        .build(),
                Document.builder()
                        .text("VectorStore is Spring AI's abstraction for embedding, storing, and similarity-"
                                + "searching Documents, with implementations like PgVectorStore and SimpleVectorStore.")
                        .metadata("topic", "vector-stores")
                        .build(),
                Document.builder()
                        .text("SimpleVectorStore is an in-memory VectorStore implementation with JSON-file "
                                + "persistence via save(File) and load(File), needing no external database.")
                        .metadata("topic", "vector-stores")
                        .build(),
                Document.builder()
                        .text("Retrieval-Augmented Generation, or RAG, grounds an LLM's answer in documents "
                                + "retrieved from a knowledge base, rather than relying only on the model's training data.")
                        .metadata("topic", "rag")
                        .build(),
                Document.builder()
                        .text("QuestionAnswerAdvisor is a Spring AI ChatClient advisor that automatically runs a "
                                + "similarity search against a VectorStore and injects the results into the prompt.")
                        .metadata("topic", "rag")
                        .build(),
                Document.builder()
                        .text("Mount Everest, on the border of Nepal and Tibet, is Earth's highest mountain "
                                + "above sea level, at 8,849 meters.")
                        .metadata("topic", "geography")
                        .build(),
                Document.builder()
                        .text("The Amazon River, flowing through South America, carries the largest discharge "
                                + "volume of water of any river in the world.")
                        .metadata("topic", "geography")
                        .build());
    }
}
