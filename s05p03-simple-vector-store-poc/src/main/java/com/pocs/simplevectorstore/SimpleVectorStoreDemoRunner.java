package com.pocs.simplevectorstore;

import java.io.File;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SimpleVectorStoreDemoRunner implements CommandLineRunner {

    @Autowired
    private SimpleVectorStoreService simpleVectorStoreService;

    @Override
    public void run(String... args) throws Exception {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (!StringUtils.hasText(apiKey)) {
            System.out.println("[s05p03] GEMINI_API_KEY not set — skipping live demo call. "
                    + "Export GEMINI_API_KEY and re-run to see a real embedded similarity search.");
            return;
        }
        try {
            simpleVectorStoreService.add(List.of(
                    Document.builder().text("Dune is a science fiction novel about a desert planet.")
                            .metadata("genre", "sci-fi").build(),
                    Document.builder().text("The Hobbit is a fantasy novel about a reluctant adventurer.")
                            .metadata("genre", "fantasy").build(),
                    Document.builder().text("Foundation is a science fiction saga about a galactic empire.")
                            .metadata("genre", "sci-fi").build()));

            List<Document> results = simpleVectorStoreService.similaritySearch("a story set among the stars", 2);
            System.out.println("[s05p03] Unfiltered top " + results.size() + " matches:");
            results.forEach(d -> System.out.println("[s05p03]  - " + d.getText()));

            List<Document> filtered = simpleVectorStoreService.similaritySearchWithFilter(
                    "a story set among the stars", 5, simpleVectorStoreService.genreEquals("fantasy"));
            System.out.println("[s05p03] Filtered (genre=fantasy) matches: " + filtered.size());
            filtered.forEach(d -> System.out.println("[s05p03]  - " + d.getText()));

            File file = File.createTempFile("s05p03-simple-vector-store", ".json");
            simpleVectorStoreService.saveToFile(file);
            System.out.println("[s05p03] Saved vector store to " + file.getAbsolutePath());
            file.deleteOnExit();
        } catch (RuntimeException e) {
            System.out.println("[s05p03] Demo call failed, but the app started fine: " + e.getMessage());
        }
    }
}
