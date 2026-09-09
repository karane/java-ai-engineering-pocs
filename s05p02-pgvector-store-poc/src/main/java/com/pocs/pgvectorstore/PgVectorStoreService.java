package com.pocs.pgvectorstore;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.jdbc.core.JdbcTemplate;

public class PgVectorStoreService {

    private final VectorStore vectorStore;

    public PgVectorStoreService(DataSource dataSource, EmbeddingModel embeddingModel, int dimensions) {
        this(PgVectorStore.builder(new JdbcTemplate(dataSource), embeddingModel)
                .dimensions(dimensions)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .initializeSchema(true)
                .build());
    }

    PgVectorStoreService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void add(List<Document> documents) {
        vectorStore.add(documents);
    }

    public List<Document> similaritySearch(String query, int topK) {
        return vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(topK).build());
    }

    public List<Document> similaritySearch(String query, int topK, double similarityThreshold) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build());
    }

    public List<Document> similaritySearch(SearchRequest searchRequest) {
        return vectorStore.similaritySearch(searchRequest);
    }

    public VectorStore vectorStore() {
        return vectorStore;
    }
}
