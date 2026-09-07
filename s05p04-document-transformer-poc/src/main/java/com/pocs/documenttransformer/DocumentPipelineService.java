package com.pocs.documenttransformer;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class DocumentPipelineService {

    private final TokenTextSplitter tokenTextSplitter;
    private final KeywordMetadataEnricher keywordMetadataEnricher;
    private final SummaryMetadataEnricher summaryMetadataEnricher;
    private final VectorStore vectorStore;

    public DocumentPipelineService(TokenTextSplitter tokenTextSplitter, KeywordMetadataEnricher keywordMetadataEnricher,
            SummaryMetadataEnricher summaryMetadataEnricher, VectorStore vectorStore) {
        this.tokenTextSplitter = tokenTextSplitter;
        this.keywordMetadataEnricher = keywordMetadataEnricher;
        this.summaryMetadataEnricher = summaryMetadataEnricher;
        this.vectorStore = vectorStore;
    }

    public List<Document> splitEnrichAndStore(List<Document> documents) {
        List<Document> chunks = tokenTextSplitter.apply(documents);
        List<Document> enriched = applyAll(chunks, keywordMetadataEnricher, summaryMetadataEnricher);
        vectorStore.add(enriched);
        return enriched;
    }

    /** Reads documents from {@code reader}, then runs them through {@link #splitEnrichAndStore}. */
    public List<Document> ingest(DocumentReader reader) {
        return splitEnrichAndStore(reader.read());
    }

    /** Splits documents only, without enriching or storing - useful to inspect chunking in isolation. */
    public List<Document> split(List<Document> documents) {
        return tokenTextSplitter.apply(documents);
    }

    private List<Document> applyAll(List<Document> documents, DocumentTransformer... transformers) {
        List<Document> result = documents;
        for (DocumentTransformer transformer : transformers) {
            result = transformer.apply(result);
        }
        return result;
    }
}
