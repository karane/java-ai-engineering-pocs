package com.pocs.llmasjudgeevaluation;

import java.util.List;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;

/**
 * The outcome of one RAG turn: the question asked, the documents retrieved for it, and the full
 * {@link ChatResponse}. Exactly the trio {@code RelevancyEvaluator}/{@code FactCheckingEvaluator} need
 * to build an {@code EvaluationRequest}.
 */
public record RagAnswer(String question, List<Document> retrievedDocuments, ChatResponse chatResponse) {

    public String content() {
        return chatResponse.getResult().getOutput().getText();
    }
}
