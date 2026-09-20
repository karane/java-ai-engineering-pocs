package com.pocs.contextualcompression;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Live variant: real Gemini embeddings for retrieval and a real Claude call for reranking.
 * Needs both keys, since embedding and scoring are two different provider calls in this POC.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class ContextualCompressionLiveTest {

    @Autowired
    private org.springframework.ai.vectorstore.VectorStore vectorStore;

    @Autowired
    private ContextualCompressionService contextualCompressionService;

    @Test
    void rerankingWithARealClaudeCallKeepsTheOnTopicChunkAndDropsTheOffTopicOne() {
        vectorStore.add(SampleCorpus.chunkedDocuments());

        List<Document> result = contextualCompressionService.compress(
                "How does retrieval-augmented generation work?", TokenBudget.GEMINI_FLASH_CONTEXT_BUDGET_TOKENS);

        assertThat(result).isNotEmpty();
        assertThat(result).noneMatch(doc -> doc.getMetadata().getOrDefault("topic", "").equals("roman-history"));
    }
}
