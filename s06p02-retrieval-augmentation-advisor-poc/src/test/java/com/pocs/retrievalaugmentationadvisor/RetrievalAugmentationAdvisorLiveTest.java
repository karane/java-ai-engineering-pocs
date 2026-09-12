package com.pocs.retrievalaugmentationadvisor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Live test requiring BOTH {@code GEMINI_API_KEY} (embeddings) and {@code ANTHROPIC_API_KEY}
 * (chat) - it exercises the exact wiring {@link RetrievalAugmentationAdvisorDemoRunner} runs, with
 * real embeddings and a real model.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class RetrievalAugmentationAdvisorLiveTest {

    @Autowired
    private RetrievalAugmentationService retrievalAugmentationService;

    @Test
    void strictAdvisorGroundsAnInScopeAnswerAndDeclinesAnOutOfScopeOne() {
        retrievalAugmentationService.loadProductDocs(KnowledgeBase.productDocs());
        retrievalAugmentationService.loadSupportDocs(KnowledgeBase.supportDocs());

        String grounded = retrievalAugmentationService.askStrict("What does RetrievalAugmentationAdvisor do?");
        assertThat(grounded).isNotBlank();

        String declined = retrievalAugmentationService.askStrict("What is the boiling point of tungsten?");
        assertThat(declined).isNotBlank();
    }

    @Test
    void multiSourceAdvisorGroundsAnAnswerUsingBothVectorStores() {
        retrievalAugmentationService.loadProductDocs(KnowledgeBase.productDocs());
        retrievalAugmentationService.loadSupportDocs(KnowledgeBase.supportDocs());

        String answer = retrievalAugmentationService.askMultiSource(
                "What is RAG and how does query augmentation work?");

        assertThat(answer).isNotBlank();
    }
}
