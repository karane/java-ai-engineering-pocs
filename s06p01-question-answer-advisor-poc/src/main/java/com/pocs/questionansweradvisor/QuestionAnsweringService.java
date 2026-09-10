package com.pocs.questionansweradvisor;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/** Demonstrates {@link QuestionAnswerAdvisor} retrieval-augmented answers. */
@Service
public class QuestionAnsweringService {

    private final VectorStore vectorStore;

    private final ChatClient chatClient;

    public QuestionAnsweringService(VectorStore vectorStore, ChatClient chatClient) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }

    public void loadKnowledgeBase(List<Document> documents) {
        vectorStore.add(documents);
    }

    /** Grounds the answer using QuestionAnswerAdvisor's default retrieval settings. */
    public String ask(String question) {
        return chatClient.prompt()
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .user(question)
                .call()
                .content();
    }

    /** Grounds the answer using a per-query SearchRequest (topK / similarityThreshold). */
    public String askWithSearchRequest(String question, int topK, double similarityThreshold) {
        SearchRequest searchRequest = SearchRequest.builder()
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build();
        QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .build();
        return chatClient.prompt()
                .advisors(advisor)
                .user(question)
                .call()
                .content();
    }

    /** Scopes retrieval to documents whose {@code topic} metadata matches, via FILTER_EXPRESSION. */
    public String askScopedToTopic(String question, String topic) {
        return chatClient.prompt()
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "topic == '" + topic + "'"))
                .user(question)
                .call()
                .content();
    }
}
