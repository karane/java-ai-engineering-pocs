package com.pocs.ragquality;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The heart of this POC: ONE identical RAG recipe - {@code QuestionAnswerAdvisor.builder(vectorStore)} wired onto {@code ChatClient} - built twice, differing only in which concrete {@code ChatModel} backs it. */
@Configuration
public class ChatClientConfig {

    /** Matches {@link KnowledgeBaseConfig#knowledgeBaseDocuments()}'s size - see class javadoc. */
    private static final int RETRIEVE_ALL_DOCUMENTS_TOP_K = 8;

    @Bean
    public ChatClient anthropicRagChatClient(AnthropicChatModel chatModel, VectorStore vectorStore) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(ragAdvisor(vectorStore))
                .build();
    }

    @Bean
    public ChatClient geminiRagChatClient(GoogleGenAiChatModel chatModel, VectorStore vectorStore) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(ragAdvisor(vectorStore))
                .build();
    }

    static QuestionAnswerAdvisor ragAdvisor(VectorStore vectorStore) {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(RETRIEVE_ALL_DOCUMENTS_TOP_K)
                        .similarityThreshold(SearchRequest.SIMILARITY_THRESHOLD_ACCEPT_ALL)
                        .build())
                .build();
    }
}
