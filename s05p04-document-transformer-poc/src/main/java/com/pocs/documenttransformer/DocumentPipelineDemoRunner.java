package com.pocs.documenttransformer;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DocumentPipelineDemoRunner implements CommandLineRunner {

    @Autowired
    private DocumentPipelineService documentPipelineService;

    @Override
    public void run(String... args) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (!StringUtils.hasText(apiKey)) {
            System.out.println("[s05p04] GEMINI_API_KEY not set — skipping live demo call (the enrichers need "
                    + "a ChatModel). Export GEMINI_API_KEY and re-run to see the full reader->splitter->"
                    + "enricher->store pipeline.");
            return;
        }
        try {
            TikaDocumentReader reader = new TikaDocumentReader(new ClassPathResource("sample-document.txt"));
            List<Document> stored = documentPipelineService.ingest(reader);

            System.out.println("[s05p04] Pipeline produced " + stored.size() + " enriched chunk(s):");
            for (Document doc : stored) {
                System.out.println("[s05p04]  - keywords=" + doc.getMetadata().get("excerpt_keywords"));
                System.out.println("[s05p04]    summary=" + doc.getMetadata().get("section_summary"));
            }
        } catch (RuntimeException e) {
            System.out.println("[s05p04] Demo call failed, but the app started fine: " + e.getMessage());
        }
    }
}
