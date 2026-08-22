package com.pocs.chatcliententity;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ProductExtractionDemoRunner implements CommandLineRunner {

    private final ProductExtractionService productExtractionService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String apiKey;

    public ProductExtractionDemoRunner(ProductExtractionService productExtractionService) {
        this.productExtractionService = productExtractionService;
    }

    @Override
    public void run(String... args) {
        if (apiKey.isBlank()) {
            System.out.println("[s03p02] ANTHROPIC_API_KEY not set — skipping live demo calls. "
                    + "Export ANTHROPIC_API_KEY and re-run to see .entity() extraction in action.");
            return;
        }

        try {
            Product single = productExtractionService.extractOne(
                    "A rugged 32oz stainless steel water bottle that keeps drinks cold for 24 hours, "
                            + "priced around $24.99, great for hiking.");
            System.out.println("[s03p02] Single entity() extraction -> " + single);

            List<Product> many = productExtractionService.extractMany(
                    "Invent 3 different kitchen gadgets a home cook would love. "
                            + "Return them as a list of products.");
            System.out.println("[s03p02] List entity() extraction -> " + many.size() + " products:");
            many.forEach(p -> System.out.println("[s03p02]   " + p));
        } catch (RuntimeException e) {
            System.out.println("[s03p02] Demo call to Claude failed, but the app started fine: " + e.getMessage());
        }
    }
}
