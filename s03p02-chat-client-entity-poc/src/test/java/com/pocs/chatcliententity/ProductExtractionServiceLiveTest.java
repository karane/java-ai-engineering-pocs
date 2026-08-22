package com.pocs.chatcliententity;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class ProductExtractionServiceLiveTest {

    @Autowired
    private ProductExtractionService productExtractionService;

    @Test
    void extractOneParsesASingleProductFromClaude() {
        Product product = productExtractionService.extractOne(
                "A rugged 32oz stainless steel water bottle that keeps drinks cold for 24 hours, "
                        + "priced around $24.99, great for hiking.");

        assertThat(product).isNotNull();
        assertThat(product.name()).isNotBlank();
        assertThat(product.category()).isNotBlank();
        assertThat(product.description()).isNotBlank();
        assertThat(product.price()).isGreaterThan(0);
    }

    @Test
    void extractManyParsesAListOfProductsFromOneClaudeResponse() {
        List<Product> products = productExtractionService.extractMany(
                "Invent 3 different kitchen gadgets a home cook would love. "
                        + "Return them as a list of products.");

        assertThat(products).hasSize(3);
        products.forEach(p -> {
            assertThat(p.name()).isNotBlank();
            assertThat(p.category()).isNotBlank();
            assertThat(p.description()).isNotBlank();
            assertThat(p.price()).isGreaterThan(0);
        });
    }
}
