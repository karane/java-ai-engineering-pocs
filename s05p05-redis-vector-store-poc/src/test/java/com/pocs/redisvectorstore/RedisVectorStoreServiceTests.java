package com.pocs.redisvectorstore;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import static org.assertj.core.api.Assertions.assertThat;

class RedisVectorStoreServiceTests {

    @Test
    void addDelegatesToUnderlyingVectorStore() {
        RecordingVectorStore store = new RecordingVectorStore();
        RedisVectorStoreService service = new RedisVectorStoreService(store, null);

        List<Document> docs = List.of(Document.builder().text("hello").build());
        service.add(docs);

        assertThat(store.added).isEqualTo(docs);
    }

    @Test
    void similaritySearchBuildsExpectedRequest() {
        RecordingVectorStore store = new RecordingVectorStore();
        RedisVectorStoreService service = new RedisVectorStoreService(store, null);

        service.similaritySearch("find me", 3);

        assertThat(store.lastRequest.getQuery()).isEqualTo("find me");
        assertThat(store.lastRequest.getTopK()).isEqualTo(3);
    }

    private static class RecordingVectorStore implements VectorStore {

        private List<Document> added;
        private SearchRequest lastRequest;

        @Override
        public void add(List<Document> documents) {
            this.added = documents;
        }

        @Override
        public void delete(List<String> idList) {
        }

        @Override
        public void delete(Filter.Expression filterExpression) {
        }

        @Override
        public List<Document> similaritySearch(SearchRequest request) {
            this.lastRequest = request;
            return new ArrayList<>();
        }
    }
}
