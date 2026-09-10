package com.pocs.questionansweradvisor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** End-to-end live test against real Gemini embeddings and Anthropic chat completion. */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class QuestionAnsweringServiceLiveTest {

    @Autowired
    private QuestionAnsweringService questionAnsweringService;

    @Test
    void askGroundsAnswerInRetrievedKnowledgeBaseDocuments() {
        questionAnsweringService.loadKnowledgeBase(KnowledgeBase.sampleDocuments());

        String answer = questionAnsweringService.ask("What does QuestionAnswerAdvisor do?");

        assertThat(answer).isNotBlank();
        assertThat(answer.toLowerCase()).containsAnyOf("vector", "retriev", "similarity", "document");
    }

    @Test
    void askScopedToUnrelatedTopicCannotBeGroundedInSpringAiDocuments() {
        questionAnsweringService.loadKnowledgeBase(KnowledgeBase.sampleDocuments());

        String answer = questionAnsweringService.askScopedToTopic("What does QuestionAnswerAdvisor do?", "geography");

        assertThat(answer).isNotBlank();
        // Scoped to "geography" only - the model has nothing about QuestionAnswerAdvisor to work
        // with, so it should say it can't answer rather than describing the RAG advisor correctly.
        assertThat(answer.toLowerCase()).doesNotContain("similarity search");
    }
}
