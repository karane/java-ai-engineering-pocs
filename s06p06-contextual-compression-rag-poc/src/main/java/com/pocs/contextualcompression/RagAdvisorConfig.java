package com.pocs.contextualcompression;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires this POC's reranking + relevance-filtering step into a real {@link RetrievalAugmentationAdvisor} pipeline, the same advisor {@code s06p04} uses for query transformation. */
@Configuration
public class RagAdvisorConfig {

    /** Candidates scoring below this on the 0-10 reranker scale are dropped as low-relevance. */
    public static final double RELEVANCE_THRESHOLD = 3.0;

    @Bean
    public RelevanceThresholdPostProcessor relevanceThresholdPostProcessor(RerankingService rerankingService) {
        return new RelevanceThresholdPostProcessor(rerankingService, RELEVANCE_THRESHOLD);
    }

    @Bean
    public RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(VectorStore vectorStore,
            RelevanceThresholdPostProcessor relevanceThresholdPostProcessor) {
        VectorStoreDocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.0)
                .topK(ContextualCompressionService.OVER_RETRIEVAL_TOP_K)
                .build();

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .documentPostProcessors(relevanceThresholdPostProcessor)
                .build();
    }

    @Bean
    public ChatClient ragChatClient(ChatModel chatModel, RetrievalAugmentationAdvisor retrievalAugmentationAdvisor) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(retrievalAugmentationAdvisor)
                .build();
    }
}
