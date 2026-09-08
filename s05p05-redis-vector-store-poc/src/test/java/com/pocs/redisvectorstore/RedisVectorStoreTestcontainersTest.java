package com.pocs.redisvectorstore;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import redis.clients.jedis.JedisPooled;

import static org.assertj.core.api.Assertions.assertThat;

class RedisVectorStoreTestcontainersTest {

    private static final int DIMENSIONS = 8;
    private static GenericContainer<?> redis;

    @BeforeAll
    static void startContainer() {
        Assumptions.assumeTrue(isDockerAvailable(),
                "Docker is not available in this environment - skipping the Redis Testcontainers IT. "
                        + "See README for how to run this manually against a real Redis Stack instance.");

        redis = new GenericContainer<>(DockerImageName.parse("redis/redis-stack-server:latest"))
                .withExposedPorts(6379);
        redis.start();
    }

    @AfterAll
    static void stopContainer() {
        if (redis != null) {
            redis.stop();
        }
    }

    private static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    @Test
    void addSimilaritySearchAndTtlExpiryWorkAgainstRealRedisStack() throws InterruptedException {
        JedisPooled jedis = new JedisPooled(redis.getHost(), redis.getMappedPort(6379));
        RedisVectorStoreService service = new RedisVectorStoreService(jedis, new DeterministicFakeEmbeddingModel());

        Document permanent = Document.builder().text("cats and dogs are common pets").metadata("genre", "animals").build();
        Document expiring = Document.builder().text("stock markets fluctuate daily").metadata("genre", "finance").build();

        service.add(List.of(permanent));
        service.addWithExpiry(List.of(expiring), 60);

        List<Document> results = service.similaritySearch("cats and dogs are common pets", 1);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getText()).isEqualTo("cats and dogs are common pets");

        long ttl = service.ttlSecondsRemaining(expiring.getId());
        assertThat(ttl).isGreaterThan(0).isLessThanOrEqualTo(60);

        long permanentTtl = service.ttlSecondsRemaining(permanent.getId());
        assertThat(permanentTtl).isEqualTo(-1); // -1 = no expiry set, per Jedis TTL semantics
    }

    /** Same deterministic hash-based fake used in s05p02/s05p03/s05p04. */
    private static class DeterministicFakeEmbeddingModel implements EmbeddingModel {

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            List<Embedding> embeddings = new ArrayList<>();
            int index = 0;
            for (String text : request.getInstructions()) {
                embeddings.add(new Embedding(vectorFor(text), index++));
            }
            return new EmbeddingResponse(embeddings);
        }

        @Override
        public float[] embed(Document document) {
            return vectorFor(document.getText());
        }

        private float[] vectorFor(String text) {
            float[] vector = new float[DIMENSIONS];
            for (int i = 0; i < DIMENSIONS; i++) {
                int seed = Math.floorMod(text.hashCode() + i * 7919, 1000);
                vector[i] = seed / 1000f;
            }
            return vector;
        }
    }
}
