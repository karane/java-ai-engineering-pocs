package com.pocs.documenttransformer;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class DocumentPipelineLiveTest {

    @Autowired
    private DocumentPipelineService documentPipelineService;

    @Test
    void ingestWithRealGeminiEnrichesEveryChunkWithKeywordsAndSummary() {
        TikaDocumentReader reader = new TikaDocumentReader(new ClassPathResource("sample-document.txt"));
        List<Document> stored = documentPipelineService.ingest(reader);

        assertThat(stored).isNotEmpty();
        for (Document doc : stored) {
            assertThat((String) doc.getMetadata().get("excerpt_keywords")).isNotBlank();
            assertThat((String) doc.getMetadata().get("section_summary")).isNotBlank();
        }
    }
}
