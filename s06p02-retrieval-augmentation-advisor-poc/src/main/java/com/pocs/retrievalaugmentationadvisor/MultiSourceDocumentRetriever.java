package com.pocs.retrievalaugmentationadvisor;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;

/** A {@link DocumentRetriever} that queries two independent {@link DocumentRetriever}s (typically two {@code VectorStoreDocumentRetriever}s over two different {@code VectorStore}s) for the same query and merges their results with a {@link DocumentJoiner}. */
public class MultiSourceDocumentRetriever implements DocumentRetriever {

    private final List<DocumentRetriever> retrievers;

    private final DocumentJoiner documentJoiner;

    public MultiSourceDocumentRetriever(List<DocumentRetriever> retrievers, DocumentJoiner documentJoiner) {
        this.retrievers = List.copyOf(retrievers);
        this.documentJoiner = documentJoiner;
    }

    @Override
    public List<Document> retrieve(Query query) {
        List<List<Document>> resultsPerRetriever = retrievers.stream().map(retriever -> retriever.retrieve(query))
                .toList();
        // DocumentJoiner is keyed by Query -> List of result lists; here every result list came from
        // the SAME query but a DIFFERENT retriever/data source, which is exactly the "multiple data
        // sources" half of what DocumentJoiner's own javadoc describes it as merging.
        return documentJoiner.join(Map.of(query, resultsPerRetriever));
    }
}
