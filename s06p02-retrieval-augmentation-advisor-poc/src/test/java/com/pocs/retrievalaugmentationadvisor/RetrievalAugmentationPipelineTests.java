package com.pocs.retrievalaugmentationadvisor;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;

import static org.assertj.core.api.Assertions.assertThat;

/** Fully offline, end-to-end tests of the {@code ChatClient.prompt().advisors(RetrievalAugmentationAdvisor)} pipeline: real {@code SimpleVectorStore}s + a real {@code RetrievalAugmentationAdvisor} + {@code VectorStoreDocumentRetriever}/{@code ContextualQueryAugmenter}/{@code DocumentJoiner}, with only the final LLM call replaced by {@link StubEchoChatModel}, which echoes back whatever prompt the advisor produced. */
class RetrievalAugmentationPipelineTests {

    private static final Document RAG_DOC = Document.builder().id("rag-def")
            .text("Retrieval-Augmented Generation, or RAG, grounds an LLM's answer in documents retrieved "
                    + "from a knowledge base.")
            .build();

    private static final Document ADVISOR_DOC = Document.builder().id("advisor-def")
            .text("RetrievalAugmentationAdvisor implements the full modular RAG pipeline as a ChatClient advisor.")
            .build();

    private VectorStore newStoreWith(Document... documents) {
        VectorStore store = SimpleVectorStore.builder(new DeterministicFakeEmbeddingModel()).build();
        store.add(List.of(documents));
        return store;
    }

    private ChatClient echoChatClient() {
        return ChatClient.builder(new StubEchoChatModel()).build();
    }

    @Test
    void strictAdvisorInjectsRetrievedDocumentTextIntoThePromptForAnInScopeQuestion() {
        VectorStore store = newStoreWith(RAG_DOC, ADVISOR_DOC);
        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(store).similarityThreshold(0.0).topK(4).build();
        RetrievalAugmentationAdvisor strictAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .build();

        String echoed = echoChatClient().prompt()
                .advisors(strictAdvisor)
                .user("What is RAG?")
                .call()
                .content();

        assertThat(echoed).contains("Retrieval-Augmented Generation");
    }

    @Test
    void strictAdvisorDeclinesInsteadOfForwardingAnOutOfScopeQuestion() {
        // An empty vector store guarantees zero matches, simulating a question with no relevant
        // documents anywhere in the knowledge base.
        VectorStore emptyStore = SimpleVectorStore.builder(new DeterministicFakeEmbeddingModel()).build();
        DocumentRetriever noMatchRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(emptyStore).similarityThreshold(0.0).topK(4).build();
        RetrievalAugmentationAdvisor strictAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(noMatchRetriever)
                .build();

        String echoed = echoChatClient().prompt()
                .advisors(strictAdvisor)
                .user("What is the boiling point of tungsten?")
                .call()
                .content();

        assertThat(echoed).contains("outside your knowledge base").doesNotContain("tungsten");
    }

    @Test
    void permissiveAdvisorForwardsAnOutOfScopeQuestionUnchanged() {
        VectorStore emptyStore = SimpleVectorStore.builder(new DeterministicFakeEmbeddingModel()).build();
        DocumentRetriever noMatchRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(emptyStore).similarityThreshold(0.0).topK(4).build();
        RetrievalAugmentationAdvisor permissiveAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(noMatchRetriever)
                .queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(true).build())
                .build();

        String echoed = echoChatClient().prompt()
                .advisors(permissiveAdvisor)
                .user("What is the boiling point of tungsten?")
                .call()
                .content();

        // Unlike the strict advisor, the original question reaches the model verbatim.
        assertThat(echoed).contains("tungsten").doesNotContain("outside your knowledge base");
    }

    @Test
    void multiSourceAdvisorGroundsTheAnswerWithDocumentsMergedFromBothVectorStores() {
        VectorStore productStore = newStoreWith(ADVISOR_DOC);
        VectorStore supportStore = newStoreWith(RAG_DOC);

        DocumentRetriever productRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(productStore).similarityThreshold(0.0).topK(4).build();
        DocumentRetriever supportRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(supportStore).similarityThreshold(0.0).topK(4).build();
        MultiSourceDocumentRetriever multiSourceRetriever = new MultiSourceDocumentRetriever(
                List.of(productRetriever, supportRetriever), new ConcatenationDocumentJoiner());

        RetrievalAugmentationAdvisor multiSourceAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(multiSourceRetriever)
                .build();

        String echoed = echoChatClient().prompt()
                .advisors(multiSourceAdvisor)
                .user("What is RAG and what implements it?")
                .call()
                .content();

        // Neither vector store alone has both facts - only the DocumentJoiner-merged result does.
        assertThat(echoed).contains("Retrieval-Augmented Generation")
                .contains("RetrievalAugmentationAdvisor implements the full modular RAG pipeline");
    }
}
