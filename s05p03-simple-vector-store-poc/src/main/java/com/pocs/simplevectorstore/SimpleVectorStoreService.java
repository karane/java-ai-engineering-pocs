package com.pocs.simplevectorstore;

import java.io.File;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

@Service
public class SimpleVectorStoreService {

    private final SimpleVectorStore simpleVectorStore;

    public SimpleVectorStoreService(VectorStore vectorStore) {
        this.simpleVectorStore = (SimpleVectorStore) vectorStore;
    }

    public void add(List<Document> documents) {
        simpleVectorStore.add(documents);
    }

    public List<Document> similaritySearch(String query, int topK) {
        return simpleVectorStore.similaritySearch(SearchRequest.builder().query(query).topK(topK).build());
    }

    /** Similarity search restricted to documents whose metadata matches the given filter. */
    public List<Document> similaritySearchWithFilter(String query, int topK, Filter.Expression filterExpression) {
        return simpleVectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(SearchRequest.SIMILARITY_THRESHOLD_ACCEPT_ALL)
                .filterExpression(filterExpression)
                .build());
    }

    /** Builds a metadata filter expression: {@code genre == value}. */
    public Filter.Expression genreEquals(String value) {
        return new FilterExpressionBuilder().eq("genre", value).build();
    }

    public void saveToFile(File file) {
        simpleVectorStore.save(file);
    }

    public void loadFromFile(File file) {
        simpleVectorStore.load(file);
    }
}
