package com.pocs.querytransformation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/** Wraps Spring AI's three pre-retrieval query transformers — {@link CompressionQueryTransformer}, {@link RewriteQueryTransformer}, and {@link MultiQueryExpander} — plus a small multi-query fan-out retrieval flow against a {@link VectorStore} that deduplicates results across query variants. */
@Service
public class QueryTransformationService {

    private final VectorStore vectorStore;

    public QueryTransformationService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /** Condenses a multi-turn conversation history + follow-up question into one standalone query. */
    public Query compressConversation(ChatClient.Builder chatClientBuilder, List<Message> history, String followUp) {
        CompressionQueryTransformer transformer = CompressionQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .build();
        Query query = Query.builder().text(followUp).history(history).build();
        return transformer.transform(query);
    }

    /** Rewrites a verbose/ambiguous user query into one optimized for vector-store search. */
    public Query rewriteForRetrieval(ChatClient.Builder chatClientBuilder, String query) {
        RewriteQueryTransformer transformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .build();
        return transformer.transform(new Query(query));
    }

    /** Fans a single query out into {@code numberOfQueries} variant phrasings. */
    public List<Query> expandQuery(ChatClient.Builder chatClientBuilder, String query, int numberOfQueries) {
        MultiQueryExpander expander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClientBuilder)
                .numberOfQueries(numberOfQueries)
                .includeOriginal(true)
                .build();
        return expander.expand(new Query(query));
    }

    /** Expands a single query into variants, retrieves against the vector store for each variant, and merges the results deduplicated by document id — a document surfaced by more than one variant is kept only once. */
    public List<Document> multiQueryRetrieveDeduped(
            ChatClient.Builder chatClientBuilder, String query, int numberOfQueries, int topKPerQuery) {
        List<Query> variants = expandQuery(chatClientBuilder, query, numberOfQueries);

        VectorStoreDocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(topKPerQuery)
                .build();

        Map<String, Document> deduped = new LinkedHashMap<>();
        for (Query variant : variants) {
            for (Document document : retriever.retrieve(variant)) {
                deduped.merge(document.getId(), document,
                        (existing, incoming) -> existing.getScore() != null
                                        && (incoming.getScore() == null || existing.getScore() >= incoming.getScore())
                                ? existing : incoming);
            }
        }
        return List.copyOf(deduped.values());
    }
}
