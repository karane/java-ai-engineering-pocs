package com.pocs.questionansweradvisor;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;

import static org.assertj.core.api.Assertions.assertThat;

/** Fully offline tests of {@link QuestionAnswerAdvisor}'s retrieval mechanics. */
class QuestionAnswerAdvisorMechanicsTests {

    private static final AdvisorChain NOOP_CHAIN = new AdvisorChain() {
    };

    private VectorStore newLoadedVectorStore() {
        VectorStore vectorStore = SimpleVectorStore.builder(new DeterministicFakeEmbeddingModel()).build();
        vectorStore.add(KnowledgeBase.sampleDocuments());
        return vectorStore;
    }

    @Test
    void defaultAdvisorAugmentsUserPromptWithRetrievedDocumentContext() {
        VectorStore vectorStore = newLoadedVectorStore();
        QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore).build();

        ChatClientRequest request = ChatClientRequest.builder()
                .prompt(new Prompt("QuestionAnswerAdvisor is a Spring AI ChatClient advisor that "
                        + "automatically runs a similarity search against a VectorStore and injects "
                        + "the results into the prompt."))
                .context(Map.of())
                .build();

        ChatClientRequest augmented = advisor.before(request, NOOP_CHAIN);

        // The augmented user text must contain the retrieved document's text - that's the whole
        // point of QuestionAnswerAdvisor: grounding the prompt in retrieved context.
        assertThat(augmented.prompt().getUserMessage().getText()).contains("Context information is below");
        assertThat(augmented.prompt().getUserMessage().getText()).contains("automatically runs a similarity search");

        @SuppressWarnings("unchecked")
        List<Document> retrieved = (List<Document>) augmented.context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        assertThat(retrieved).isNotEmpty();
    }

    @Test
    void perQuerySearchRequestTopKLimitsRetrievedDocumentCount() {
        VectorStore vectorStore = newLoadedVectorStore();

        SearchRequest wideOpen = SearchRequest.builder()
                .topK(8)
                .similarityThreshold(SearchRequest.SIMILARITY_THRESHOLD_ACCEPT_ALL)
                .build();
        SearchRequest narrow = SearchRequest.builder()
                .topK(1)
                .similarityThreshold(SearchRequest.SIMILARITY_THRESHOLD_ACCEPT_ALL)
                .build();

        QuestionAnswerAdvisor wideAdvisor = QuestionAnswerAdvisor.builder(vectorStore).searchRequest(wideOpen).build();
        QuestionAnswerAdvisor narrowAdvisor = QuestionAnswerAdvisor.builder(vectorStore).searchRequest(narrow).build();

        ChatClientRequest request = ChatClientRequest.builder()
                .prompt(new Prompt("Tell me about Spring AI's retrieval-augmented generation support."))
                .context(Map.of())
                .build();

        ChatClientRequest wideResult = wideAdvisor.before(request, NOOP_CHAIN);
        ChatClientRequest narrowResult = narrowAdvisor.before(request, NOOP_CHAIN);

        List<?> wideDocs = (List<?>) wideResult.context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        List<?> narrowDocs = (List<?>) narrowResult.context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);

        assertThat(narrowDocs).hasSize(1);
        assertThat(wideDocs.size()).isGreaterThan(narrowDocs.size());
    }

    @Test
    void filterExpressionParamScopesRetrievalToMatchingTopicOnly() {
        VectorStore vectorStore = newLoadedVectorStore();
        SearchRequest acceptAll = SearchRequest.builder()
                .topK(8)
                .similarityThreshold(SearchRequest.SIMILARITY_THRESHOLD_ACCEPT_ALL)
                .build();
        QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore).searchRequest(acceptAll).build();

        ChatClientRequest request = ChatClientRequest.builder()
                .prompt(new Prompt("What is QuestionAnswerAdvisor and what does it do?"))
                .context(QuestionAnswerAdvisor.FILTER_EXPRESSION, "topic == 'geography'")
                .build();

        ChatClientRequest result = advisor.before(request, NOOP_CHAIN);

        @SuppressWarnings("unchecked")
        List<Document> retrieved = (List<Document>) result.context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);

        // The question is about a Spring AI/RAG concept, but retrieval was scoped to "geography" -
        // proving the filter genuinely restricts the candidate set (not just re-ranks it): only the
        // geography documents come back, so the augmented prompt can't be grounded in the RAG docs.
        assertThat(retrieved).isNotEmpty();
        assertThat(retrieved).allSatisfy(d -> assertThat(d.getMetadata().get("topic")).isEqualTo("geography"));
        assertThat(result.prompt().getUserMessage().getText()).doesNotContain("QuestionAnswerAdvisor is a Spring AI");
    }

    @Test
    void noFilterExpressionParamFallsBackToTheAdvisorsOwnSearchRequestFilter() {
        VectorStore vectorStore = newLoadedVectorStore();
        SearchRequest acceptAll = SearchRequest.builder()
                .topK(8)
                .similarityThreshold(SearchRequest.SIMILARITY_THRESHOLD_ACCEPT_ALL)
                .build();
        QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore).searchRequest(acceptAll).build();

        ChatClientRequest request = ChatClientRequest.builder()
                .prompt(new Prompt("What is QuestionAnswerAdvisor and what does it do?"))
                .context(Map.of())
                .build();

        ChatClientRequest result = advisor.before(request, NOOP_CHAIN);

        @SuppressWarnings("unchecked")
        List<Document> retrieved = (List<Document>) result.context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);

        // Unscoped: documents from more than one topic should be eligible to come back.
        assertThat(retrieved.stream().map(d -> d.getMetadata().get("topic")).distinct().count()).isGreaterThan(1);
    }
}
