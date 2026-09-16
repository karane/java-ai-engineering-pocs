package com.pocs.querytransformation;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;

import static org.assertj.core.api.Assertions.assertThat;

/** Fully offline tests against the real Spring AI transformers ({@link org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer}, {@link org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer}, {@link org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander}) — backed by {@link FakeTransformationChatModel} instead of a real provider, so no API key or network call is needed to prove the wiring, prompt rendering, response parsing, and merge/dedup logic are all correct. */
class QueryTransformationServiceTests {

    private final ChatClient.Builder fakeChatClientBuilder = ChatClient.builder(new FakeTransformationChatModel());

    private QueryTransformationService newService() {
        VectorStore vectorStore = SimpleVectorStore.builder(new DeterministicFakeEmbeddingModel()).build();
        return new QueryTransformationService(vectorStore);
    }

    @Test
    void compressionCondensesConversationHistoryAndFollowUpIntoStandaloneQuery() {
        QueryTransformationService service = newService();
        List<Message> history = List.of(
                new UserMessage("Tell me about the Eiffel Tower."),
                new AssistantMessage("The Eiffel Tower is a wrought-iron lattice tower in Paris, France."));

        Query standalone = service.compressConversation(fakeChatClientBuilder, history, "How tall is it?");

        assertThat(standalone.text()).contains("Eiffel Tower").contains("How tall is it?");
    }

    @Test
    void rewriteStripsConversationalFillerFromTheQuery() {
        QueryTransformationService service = newService();

        Query rewritten = service.rewriteForRetrieval(
                fakeChatClientBuilder, "Um, can you please tell me about the population of Paris?");

        assertThat(rewritten.text())
                .contains("population of Paris")
                .doesNotContainIgnoringCase("please")
                .doesNotContainIgnoringCase("um,");
    }

    @Test
    void multiQueryExpanderFansOutIntoDistinctVariantsIncludingTheOriginal() {
        QueryTransformationService service = newService();

        List<Query> variants = service.expandQuery(fakeChatClientBuilder, "Paris landmarks", 3);

        assertThat(variants).hasSize(4);
        assertThat(variants).extracting(Query::text).contains("Paris landmarks");
        assertThat(variants).extracting(Query::text).doesNotHaveDuplicates();
    }

    @Test
    void multiQueryFanOutRetrievalDeduplicatesDocumentsAcrossVariants() {
        QueryTransformationService service = newService();
        // Same VectorStore instance the service was built with, populated directly here.
        VectorStore vectorStore = SimpleVectorStore.builder(new DeterministicFakeEmbeddingModel()).build();
        QueryTransformationService serviceWithStore = new QueryTransformationService(vectorStore);
        vectorStore.add(List.of(
                Document.builder().id("eiffel").text("The Eiffel Tower is a landmark in Paris.").build(),
                Document.builder().id("louvre").text("The Louvre is a museum in Paris.").build(),
                Document.builder().id("fuji").text("Mount Fuji is a mountain in Japan.").build()));

        List<Document> results = serviceWithStore.multiQueryRetrieveDeduped(
                fakeChatClientBuilder, "famous places in Paris", 3, 3);

        assertThat(results).isNotEmpty();
        // No duplicate ids even though the same documents were retrieved across multiple variants.
        assertThat(results).extracting(Document::getId).doesNotHaveDuplicates();
    }
}
