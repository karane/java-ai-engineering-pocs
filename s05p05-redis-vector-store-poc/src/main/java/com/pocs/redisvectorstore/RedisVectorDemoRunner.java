package com.pocs.redisvectorstore;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import redis.clients.jedis.JedisPooled;

@Component
public class RedisVectorDemoRunner implements CommandLineRunner {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Override
    public void run(String... args) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        String redisHost = System.getenv("REDIS_HOST");
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(redisHost)) {
            System.out.println("[s05p05] GEMINI_API_KEY and/or REDIS_HOST not set — skipping live demo. "
                    + "Export both (see README) and re-run to see RedisVectorStore against a real Redis Stack.");
            return;
        }
        try {
            int redisPort = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
            JedisPooled jedis = new JedisPooled(redisHost, redisPort);

            RedisVectorStoreService service = new RedisVectorStoreService(jedis, embeddingModel);

            service.add(List.of(
                    Document.builder().text("Spring AI simplifies building AI applications in Java.")
                            .metadata("genre", "tech").build(),
                    Document.builder().text("Redis Stack adds search and JSON capabilities to Redis.")
                            .metadata("genre", "tech").build(),
                    Document.builder().text("The Amazon rainforest is the largest tropical rainforest.")
                            .metadata("genre", "geography").build()));

            List<Document> results = service.similaritySearch("How do I search vectors in Redis?", 2);
            System.out.println("[s05p05] Top " + results.size() + " matches for the query:");
            results.forEach(d -> System.out.println("[s05p05]  - " + d.getText() + " (score=" + d.getScore() + ")"));
        } catch (RuntimeException e) {
            System.out.println("[s05p05] Demo call failed, but the app started fine: " + e.getMessage());
        }
    }
}
