package com.pocs.contextualcompression;

import org.springframework.ai.document.Document;

/** A retrieved {@link Document} paired with a 0-10 cross-encoder-style relevance score. */
public record ScoredDocument(Document document, double score) {
}
