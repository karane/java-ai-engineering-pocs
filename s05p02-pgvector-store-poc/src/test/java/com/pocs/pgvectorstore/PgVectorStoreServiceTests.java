package com.pocs.pgvectorstore;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import static org.assertj.core.api.Assertions.assertThat;

class PgVectorStoreServiceTests {

    @Test
    void addDelegatesToUnderlyingVectorStore() {
        RecordingVectorStore store = new RecordingVectorStore();
        PgVectorStoreService service = new PgVectorStoreService(store);

        List<Document> docs = List.of(Document.builder().text("hello").build());
        service.add(docs);

        assertThat(store.added).isEqualTo(docs);
    }

    @Test
    void similaritySearchWithTopKBuildsExpectedRequest() {
        RecordingVectorStore store = new RecordingVectorStore();
        PgVectorStoreService service = new PgVectorStoreService(store);

        service.similaritySearch("find me", 3);

        assertThat(store.lastRequest.getQuery()).isEqualTo("find me");
        assertThat(store.lastRequest.getTopK()).isEqualTo(3);
    }

    @Test
    void similaritySearchWithThresholdBuildsExpectedRequest() {
        RecordingVectorStore store = new RecordingVectorStore();
        PgVectorStoreService service = new PgVectorStoreService(store);

        service.similaritySearch("find me", 5, 0.85);

        assertThat(store.lastRequest.getTopK()).isEqualTo(5);
        assertThat(store.lastRequest.getSimilarityThreshold()).isEqualTo(0.85);
    }

    /** Minimal in-memory VectorStore stub that just records what it was asked to do. */
    private static class RecordingVectorStore implements VectorStore {

        private List<Document> added;
        private SearchRequest lastRequest;

        @Override
        public void add(List<Document> documents) {
            this.added = documents;
        }

        @Override
        public void delete(List<String> idList) {
            // not exercised by this POC's tests
        }

        @Override
        public void delete(Filter.Expression filterExpression) {
            // not exercised by this POC's tests
        }

        @Override
        public List<Document> similaritySearch(SearchRequest request) {
            this.lastRequest = request;
            return new ArrayList<>();
        }
    }
}
