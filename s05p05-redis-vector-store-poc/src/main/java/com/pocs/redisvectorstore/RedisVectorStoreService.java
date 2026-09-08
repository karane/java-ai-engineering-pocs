package com.pocs.redisvectorstore;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;

import redis.clients.jedis.JedisPooled;

public class RedisVectorStoreService {

    /** Matches RedisVectorStore.DEFAULT_PREFIX - not exposed publicly, so duplicated here. */
    private static final String KEY_PREFIX = "embedding:";

    private final VectorStore vectorStore;
    private final JedisPooled jedis;

    public RedisVectorStoreService(JedisPooled jedis, EmbeddingModel embeddingModel) {
        this.jedis = jedis;
        RedisVectorStore redisVectorStore = RedisVectorStore.builder(jedis, embeddingModel)
                .vectorAlgorithm(RedisVectorStore.Algorithm.HNSW)
                .metadataFields(RedisVectorStore.MetadataField.tag("genre"))
                .initializeSchema(true)
                .build();
        redisVectorStore.afterPropertiesSet();
        this.vectorStore = redisVectorStore;
    }

    /** Test-only constructor: a hand-rolled {@link VectorStore} stub, no real Redis needed. */
    RedisVectorStoreService(VectorStore vectorStore, JedisPooled jedis) {
        this.vectorStore = vectorStore;
        this.jedis = jedis;
    }

    public void add(List<Document> documents) {
        vectorStore.add(documents);
    }

    /** Adds documents, then sets a Redis key TTL (in seconds) on each one's JSON key. */
    public void addWithExpiry(List<Document> documents, long ttlSeconds) {
        vectorStore.add(documents);
        for (Document document : documents) {
            jedis.expire(KEY_PREFIX + document.getId(), ttlSeconds);
        }
    }

    /** Remaining TTL (seconds) for a document's Redis key, or -1/-2 per Jedis TTL semantics. */
    public long ttlSecondsRemaining(String documentId) {
        return jedis.ttl(KEY_PREFIX + documentId);
    }

    public List<Document> similaritySearch(String query, int topK) {
        return vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(topK).build());
    }

    public List<Document> similaritySearch(SearchRequest searchRequest) {
        return vectorStore.similaritySearch(searchRequest);
    }

    public VectorStore vectorStore() {
        return vectorStore;
    }
}
