package com.pocs.simplevectorstore;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleVectorStoreServiceTests {

    private SimpleVectorStoreService newService() {
        VectorStore vectorStore = SimpleVectorStore.builder(new DeterministicFakeEmbeddingModel()).build();
        return new SimpleVectorStoreService(vectorStore);
    }

    @Test
    void similaritySearchReturnsTheExactTextMatchFirst() {
        SimpleVectorStoreService service = newService();
        service.add(List.of(
                Document.builder().text("cats and dogs are common pets").metadata("genre", "animals").build(),
                Document.builder().text("stock markets fluctuate daily").metadata("genre", "finance").build()));

        List<Document> results = service.similaritySearch("cats and dogs are common pets", 1);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getText()).isEqualTo("cats and dogs are common pets");
    }

    @Test
    void similaritySearchWithFilterOnlyReturnsMatchingMetadata() {
        SimpleVectorStoreService service = newService();
        service.add(List.of(
                Document.builder().text("Dune is a science fiction novel.").metadata("genre", "sci-fi").build(),
                Document.builder().text("The Hobbit is a fantasy novel.").metadata("genre", "fantasy").build(),
                Document.builder().text("Foundation is a science fiction saga.").metadata("genre", "sci-fi").build()));

        Filter.Expression sciFiOnly = service.genreEquals("sci-fi");
        List<Document> results = service.similaritySearchWithFilter("a story", 10, sciFiOnly);

        assertThat(results).hasSize(2);
        assertThat(results).allSatisfy(d -> assertThat(d.getMetadata().get("genre")).isEqualTo("sci-fi"));
    }

    @Test
    void saveThenLoadRoundTripsAllDocuments(@org.junit.jupiter.api.io.TempDir File tempDir) {
        SimpleVectorStoreService service = newService();
        service.add(List.of(
                Document.builder().text("first document").build(),
                Document.builder().text("second document").build()));

        File file = new File(tempDir, "vector-store.json");
        service.saveToFile(file);
        assertThat(file).exists();

        SimpleVectorStoreService reloaded = newService();
        reloaded.loadFromFile(file);

        List<Document> results = reloaded.similaritySearch("first document", 2);
        assertThat(results).extracting(Document::getText)
                .containsExactlyInAnyOrder("first document", "second document");
    }
}
