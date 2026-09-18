package com.pocs.ragevaluation;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

/** The small RAG pipeline under evaluation: a {@link ChatClient} wrapped with a {@link QuestionAnswerAdvisor} so every question is answered using documents retrieved from a {@link VectorStore}. */
public class RagAnswerService {

    private final ChatClient chatClient;

    public RagAnswerService(ChatModel chatModel, VectorStore vectorStore) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        // The bag-of-words hashing embedding gives genuine (if crude)
                        // keyword-overlap similarity, so topK(1) reliably retrieves the
                        // single relevant document for each golden question in this
                        // POC's small knowledge base - see DeterministicFakeEmbeddingModel.
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
