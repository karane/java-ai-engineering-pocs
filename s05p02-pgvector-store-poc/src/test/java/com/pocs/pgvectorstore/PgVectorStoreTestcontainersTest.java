package com.pocs.pgvectorstore;

import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

class PgVectorStoreTestcontainersTest {

    private static final int DIMENSIONS = 8;

    private static PostgreSQLContainer<?> postgres;

    @BeforeAll
    static void startContainer() {
        Assumptions.assumeTrue(isDockerAvailable(),
                "Docker is not available in this environment - skipping the pgvector Testcontainers IT. "
                        + "See README for how to run this manually against a real Postgres+pgvector instance.");

        postgres = new PostgreSQLContainer<>(DockerImageName.parse("pgvector/pgvector:pg16")
                .asCompatibleSubstituteFor("postgres"));
        postgres.start();
    }

    private static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    @AfterAll
    static void stopContainer() {
        if (postgres != null) {
            postgres.stop();
        }
    }

    @Test
    void addAndSimilaritySearchWorkAgainstRealPgvector() {
        DataSource dataSource = new SimpleDriverDataSource(new org.postgresql.Driver(),
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());

        PgVectorStoreService service = new PgVectorStoreService(dataSource, new DeterministicFakeEmbeddingModel(), DIMENSIONS);

        service.add(List.of(
                Document.builder().text("cats and dogs are common pets").metadata("topic", "animals").build(),
                Document.builder().text("stock markets fluctuate daily").metadata("topic", "finance").build()));

        List<Document> results = service.similaritySearch("cats and dogs are common pets", 1);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getText()).isEqualTo("cats and dogs are common pets");
    }

    private static class DeterministicFakeEmbeddingModel implements EmbeddingModel {

        @Override
        public EmbeddingResponse call(org.springframework.ai.embedding.EmbeddingRequest request) {
            List<org.springframework.ai.embedding.Embedding> embeddings = new java.util.ArrayList<>();
            int index = 0;
            for (String text : request.getInstructions()) {
                embeddings.add(new org.springframework.ai.embedding.Embedding(vectorFor(text), index++));
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
                // floorMod (not %) keeps every component non-negative regardless of the
                // sign of String.hashCode() - plain % can make two different texts land
                // as near-opposite vectors (cosine ~ -1), which similaritySearch then
                // silently filters out via its default [0,1] similarity threshold.
                int seed = Math.floorMod(text.hashCode() + i * 7919, 1000);
                vector[i] = seed / 1000f;
            }
            return vector;
        }
    }
}
