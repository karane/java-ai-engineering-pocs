package com.pocs.retrievalaugmentationadvisor;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;

import static org.assertj.core.api.Assertions.assertThat;

/** Fully offline tests of {@link MultiSourceDocumentRetriever}: two {@code SimpleVectorStore}s backed by a deterministic fake {@code EmbeddingModel}, proving {@code DocumentJoiner} actually merges results from two different retrievers into one deduplicated, score-ranked list. */
class MultiSourceDocumentRetrieverTests {

    private VectorStore newStore() {
        return SimpleVectorStore.builder(new DeterministicFakeEmbeddingModel()).build();
    }

    @Test
    void mergesAndDeduplicatesDocumentsSharedAcrossBothSources() {
        VectorStore productStore = newStore();
        VectorStore supportStore = newStore();

        Document shared = Document.builder().id("shared-doc")
                .text("Retrieval-Augmented Generation grounds answers in retrieved documents.").build();
        Document productOnly = Document.builder().id("product-only")
                .text("RetrievalAugmentationAdvisor implements the full modular RAG pipeline.").build();
        Document supportOnly = Document.builder().id("support-only")
                .text("ContextualQueryAugmenter rewrites the query with retrieved context.").build();

        productStore.add(List.of(shared, productOnly));
        supportStore.add(List.of(shared, supportOnly));

        DocumentRetriever productRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(productStore).similarityThreshold(0.0).topK(10).build();
        DocumentRetriever supportRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(supportStore).similarityThreshold(0.0).topK(10).build();

        MultiSourceDocumentRetriever multiSource = new MultiSourceDocumentRetriever(
                List.of(productRetriever, supportRetriever), new ConcatenationDocumentJoiner());

        List<Document> merged = multiSource.retrieve(new Query("What is RAG?"));

        // Three distinct ids across both stores (shared + product-only + support-only), even though
        // four (store, document) pairs were retrieved in total - proving the shared document was
        // deduplicated by id rather than appearing twice.
        assertThat(merged).extracting(Document::getId)
                .containsExactlyInAnyOrder("shared-doc", "product-only", "support-only");
    }

    @Test
    void mergedResultsAreSortedByScoreDescending() {
        VectorStore productStore = newStore();
        VectorStore supportStore = newStore();

        productStore.add(List.of(Document.builder().id("a").text("cats and dogs are common pets").build()));
        supportStore.add(List.of(Document.builder().id("b").text("stock markets fluctuate daily").build()));

        DocumentRetriever productRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(productStore).similarityThreshold(0.0).topK(10).build();
        DocumentRetriever supportRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(supportStore).similarityThreshold(0.0).topK(10).build();

        MultiSourceDocumentRetriever multiSource = new MultiSourceDocumentRetriever(
                List.of(productRetriever, supportRetriever), new ConcatenationDocumentJoiner());

        List<Document> merged = multiSource.retrieve(new Query("cats and dogs are common pets"));

        assertThat(merged).isNotEmpty();
        // Scores must be non-increasing across the merged, ranked result.
        for (int i = 1; i < merged.size(); i++) {
            double previous = merged.get(i - 1).getScore() != null ? merged.get(i - 1).getScore() : 0.0;
            double current = merged.get(i).getScore() != null ? merged.get(i).getScore() : 0.0;
            assertThat(previous).isGreaterThanOrEqualTo(current);
        }
    }
}
