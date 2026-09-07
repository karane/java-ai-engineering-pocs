package com.pocs.documenttransformer;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentPipelineServiceTests {

    private VectorStore lastVectorStoreUsed;

    private DocumentPipelineService newService(String fakeChatResponse) {
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(50)
                .withMinChunkSizeChars(20)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(50)
                .build();

        FakeChatModel fakeChatModel = new FakeChatModel(fakeChatResponse);
        KeywordMetadataEnricher keywordEnricher = KeywordMetadataEnricher.builder(fakeChatModel).keywordCount(3).build();
        SummaryMetadataEnricher summaryEnricher = new SummaryMetadataEnricher(
                fakeChatModel, List.of(SummaryMetadataEnricher.SummaryType.CURRENT));

        VectorStore vectorStore = SimpleVectorStore.builder(new DeterministicFakeEmbeddingModel()).build();
        this.lastVectorStoreUsed = vectorStore;

        return new DocumentPipelineService(splitter, keywordEnricher, summaryEnricher, vectorStore);
    }

    @Test
    void ingestReadsSplitsEnrichesAndStoresTheSampleDocument() {
        DocumentPipelineService service = newService("spring, ai, java");

        TikaDocumentReader reader = new TikaDocumentReader(new ClassPathResource("sample-document.txt"));
        List<Document> stored = service.ingest(reader);

        assertThat(stored).isNotEmpty();
        for (Document doc : stored) {
            assertThat(doc.getMetadata()).containsKey("excerpt_keywords");
            assertThat(doc.getMetadata()).containsKey("section_summary");
            assertThat(doc.getMetadata().get("excerpt_keywords")).isEqualTo("spring, ai, java");
        }
    }

    @Test
    void storedChunksAreFindableViaSimilaritySearch() {
        DocumentPipelineService service = newService("keyword1, keyword2");
        TikaDocumentReader reader = new TikaDocumentReader(new ClassPathResource("sample-document.txt"));
        List<Document> stored = service.ingest(reader);

        List<Document> results = lastVectorStoreUsed.similaritySearch(
                SearchRequest.builder().query(stored.get(0).getText()).topK(1).build());

        assertThat(results).hasSize(1);
    }

    @Test
    void splitProducesMultipleChunksForALongerDocument() {
        DocumentPipelineService service = newService("n/a");
        String longText = "Spring AI makes it easy to build LLM applications. ".repeat(50);
        List<Document> chunks = service.split(List.of(Document.builder().text(longText).build()));

        assertThat(chunks.size()).isGreaterThan(1);
    }
}
