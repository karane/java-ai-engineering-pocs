package com.pocs.retrievalaugmentationadvisor;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/** Demonstrates {@link RetrievalAugmentationAdvisor}: wiring it into {@code ChatClient.prompt() .advisors(...)}, contrasting the default (strict) {@code ContextualQueryAugmenter} against a permissive ({@code allowEmptyContext=true}) one on an out-of-scope question, and merging two data sources via {@code DocumentJoiner} through {@link MultiSourceDocumentRetriever}. */
@Service
public class RetrievalAugmentationService {

    private final VectorStore productDocsVectorStore;

    private final VectorStore supportDocsVectorStore;

    private final ChatClient chatClient;

    private final RetrievalAugmentationAdvisor strictAugmentationAdvisor;

    private final RetrievalAugmentationAdvisor permissiveAugmentationAdvisor;

    private final RetrievalAugmentationAdvisor multiSourceAugmentationAdvisor;

    public RetrievalAugmentationService(
            @Qualifier("productDocsVectorStore") VectorStore productDocsVectorStore,
            @Qualifier("supportDocsVectorStore") VectorStore supportDocsVectorStore,
            ChatClient chatClient,
            RetrievalAugmentationAdvisor strictAugmentationAdvisor,
            RetrievalAugmentationAdvisor permissiveAugmentationAdvisor,
            RetrievalAugmentationAdvisor multiSourceAugmentationAdvisor) {
        this.productDocsVectorStore = productDocsVectorStore;
        this.supportDocsVectorStore = supportDocsVectorStore;
        this.chatClient = chatClient;
        this.strictAugmentationAdvisor = strictAugmentationAdvisor;
        this.permissiveAugmentationAdvisor = permissiveAugmentationAdvisor;
        this.multiSourceAugmentationAdvisor = multiSourceAugmentationAdvisor;
    }

    public void loadProductDocs(List<Document> documents) {
        productDocsVectorStore.add(documents);
    }

    public void loadSupportDocs(List<Document> documents) {
        supportDocsVectorStore.add(documents);
    }

    /** Grounds the answer, declining politely when the query is outside the product knowledge base. */
    public String askStrict(String question) {
        return chatClient.prompt()
                .advisors(strictAugmentationAdvisor)
                .user(question)
                .call()
                .content();
    }

    /** Same retriever, but falls back to the model's own knowledge when nothing relevant is found. */
    public String askPermissive(String question) {
        return chatClient.prompt()
                .advisors(permissiveAugmentationAdvisor)
                .user(question)
                .call()
                .content();
    }

    /** Grounds the answer using documents merged and deduplicated from BOTH vector stores. */
    public String askMultiSource(String question) {
        return chatClient.prompt()
                .advisors(multiSourceAugmentationAdvisor)
                .user(question)
                .call()
                .content();
    }
}
