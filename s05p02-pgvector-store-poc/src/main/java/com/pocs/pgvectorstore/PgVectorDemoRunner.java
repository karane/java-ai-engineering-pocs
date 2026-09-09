package com.pocs.pgvectorstore;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PgVectorDemoRunner implements CommandLineRunner {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Override
    public void run(String... args) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        String jdbcUrl = System.getenv("PGVECTOR_JDBC_URL");
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(jdbcUrl)) {
            System.out.println("[s05p02] GEMINI_API_KEY and/or PGVECTOR_JDBC_URL not set — skipping live "
                    + "demo. Export both (see README) and re-run to see PgVectorStore against a real Postgres.");
            return;
        }
        try {
            DriverManagerDataSource dataSource = new DriverManagerDataSource(jdbcUrl);
            dataSource.setUsername(System.getenv().getOrDefault("PGVECTOR_JDBC_USERNAME", "postgres"));
            dataSource.setPassword(System.getenv().getOrDefault("PGVECTOR_JDBC_PASSWORD", "postgres"));

            PgVectorStoreService service = new PgVectorStoreService(dataSource, embeddingModel, embeddingModel.dimensions());

            service.add(List.of(
                    Document.builder().text("Spring AI simplifies building AI applications in Java.")
                            .metadata("topic", "spring-ai").build(),
                    Document.builder().text("PostgreSQL with pgvector supports storing and searching embeddings.")
                            .metadata("topic", "database").build(),
                    Document.builder().text("The Amazon rainforest is the largest tropical rainforest.")
                            .metadata("topic", "geography").build()));

            List<Document> results = service.similaritySearch("How do I store vector embeddings?", 2);
            System.out.println("[s05p02] Top " + results.size() + " matches for the query:");
            results.forEach(d -> System.out.println("[s05p02]  - " + d.getText() + " (score=" + d.getScore() + ")"));
        } catch (RuntimeException e) {
            System.out.println("[s05p02] Demo call failed, but the app started fine: " + e.getMessage());
        }
    }
}
