package com.pocs.contextualcompression;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

/** A "cross-encoder-style" reranker: unlike a bi-encoder (which embeds the query and each document independently, then compares vectors - what {@code VectorStore.similaritySearch} already does), a cross-encoder looks at the query and one candidate document *together* and scores their relevance directly. */
@Service
public class RerankingService {

    private static final Pattern FIRST_NUMBER = Pattern.compile("-?\\d+(?:\\.\\d+)?");

    private final ChatClient chatClient;

    public RerankingService(ChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
    }

    /**
     * Scores every candidate against {@code query} and returns them sorted by descending
     * relevance score. Scoring failures (a response with no parseable number) score 0 rather
     * than throwing, so one bad candidate can't blow up the whole rerank.
     */
    public List<ScoredDocument> rerank(String query, List<Document> candidates) {
        return candidates.stream()
                .map(document -> new ScoredDocument(document, scoreRelevance(query, document.getText())))
                .sorted(Comparator.comparingDouble(ScoredDocument::score).reversed())
                .toList();
    }

    private double scoreRelevance(String query, String passage) {
        String response = chatClient.prompt()
                .user(u -> u.text("""
                        Score how relevant the PASSAGE is to the QUERY, on an integer scale from 0
                        (completely unrelated) to 10 (directly and fully answers the query).
                        Reply with ONLY the integer score and nothing else - no words, no punctuation.

                        QUERY: {query}
                        PASSAGE: {passage}
                        """)
                        .param("query", query)
                        .param("passage", passage))
                .call()
                .content();
        return parseScore(response);
    }

    private double parseScore(String response) {
        if (response == null) {
            return 0;
        }
        Matcher matcher = FIRST_NUMBER.matcher(response);
        if (!matcher.find()) {
            return 0;
        }
        return Double.parseDouble(matcher.group());
    }
}
