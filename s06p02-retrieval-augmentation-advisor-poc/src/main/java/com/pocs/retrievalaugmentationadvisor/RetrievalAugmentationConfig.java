package com.pocs.retrievalaugmentationadvisor;

import java.util.List;

import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetrievalAugmentationConfig {

    @Bean
    public RetrievalAugmentationAdvisor strictAugmentationAdvisor(
            @Qualifier("productDocsVectorStore") VectorStore productDocsVectorStore) {
        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(productDocsVectorStore)
                .similarityThreshold(0.5)
                .topK(4)
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .build();
    }

    @Bean
    public RetrievalAugmentationAdvisor permissiveAugmentationAdvisor(
            @Qualifier("productDocsVectorStore") VectorStore productDocsVectorStore) {
        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(productDocsVectorStore)
                .similarityThreshold(0.5)
                .topK(4)
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(true).build())
                .build();
    }

    @Bean
    public RetrievalAugmentationAdvisor multiSourceAugmentationAdvisor(
            @Qualifier("productDocsVectorStore") VectorStore productDocsVectorStore,
            @Qualifier("supportDocsVectorStore") VectorStore supportDocsVectorStore) {
        DocumentRetriever productRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(productDocsVectorStore)
                .similarityThreshold(0.5)
                .topK(4)
                .build();
        DocumentRetriever supportRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(supportDocsVectorStore)
                .similarityThreshold(0.5)
                .topK(4)
                .build();
        MultiSourceDocumentRetriever multiSourceRetriever = new MultiSourceDocumentRetriever(
                List.of(productRetriever, supportRetriever), new ConcatenationDocumentJoiner());
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(multiSourceRetriever)
                .build();
    }
}
