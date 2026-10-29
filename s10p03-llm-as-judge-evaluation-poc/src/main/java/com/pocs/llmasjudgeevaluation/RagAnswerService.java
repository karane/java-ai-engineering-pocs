package com.pocs.llmasjudgeevaluation;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

/** The small RAG pipeline under evaluation: a {@link ChatClient} wrapped with a {@link QuestionAnswerAdvisor}. */
public class RagAnswerService {

    private final ChatClient chatClient;

    public RagAnswerService(ChatModel chatModel, VectorStore vectorStore) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder().topK(1).build())
                        .build())
                .build();
    }

    public RagAnswer answer(String question) {
        ChatResponse chatResponse = chatClient.prompt().user(question).call().chatResponse();
        List<Document> retrievedDocuments =
                chatResponse.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        return new RagAnswer(question, retrievedDocuments, chatResponse);
    }
}
