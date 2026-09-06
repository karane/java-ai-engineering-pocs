package com.pocs.simplevectorstore;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class SimpleVectorStoreLiveTest {

    @Autowired
    private SimpleVectorStoreService simpleVectorStoreService;

    @Test
    void similaritySearchWithRealGeminiEmbeddingsRanksSemanticMatchFirst() {
        simpleVectorStoreService.add(List.of(
                Document.builder().text("Dune is a science fiction novel about a desert planet.")
                        .metadata("genre", "sci-fi").build(),
                Document.builder().text("The Hobbit is a fantasy novel about a reluctant adventurer.")
                        .metadata("genre", "fantasy").build()));

        List<Document> results = simpleVectorStoreService.similaritySearch("a science fiction story", 1);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getText()).contains("Dune");
    }
}
