package com.pocs.ragevaluation;

import java.util.List;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;

/** The outcome of one RAG turn: the question asked, the documents the vector store retrieved for it, and the full {@link ChatResponse} the model produced. */
public record RagAnswer(String question, List<Document> retrievedDocuments, ChatResponse chatResponse) {

    public String content() {
        return chatResponse.getResult().getOutput().getText();
    }
}
