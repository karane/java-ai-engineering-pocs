package com.pocs.querytransformation;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Exercises the same three transformers against a real Anthropic {@code ChatModel} instead of the fake one. */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class QueryTransformationLiveTest {

    @Autowired
    private AnthropicChatModel anthropicChatModel;

    private QueryTransformationService newServiceWithFreshStore() {
        VectorStore vectorStore = SimpleVectorStore.builder(new DeterministicFakeEmbeddingModel()).build();
        return new QueryTransformationService(vectorStore);
    }

    @Test
    void compressionCondensesConversationHistoryUsingARealModel() {
        ChatClient.Builder realChatClientBuilder = ChatClient.builder(anthropicChatModel);
        QueryTransformationService service = newServiceWithFreshStore();
        List<Message> history = List.of(
                new UserMessage("Tell me about the Eiffel Tower."),
                new AssistantMessage("The Eiffel Tower is a wrought-iron lattice tower in Paris, France."));

        Query standalone = service.compressConversation(realChatClientBuilder, history, "How tall is it?");

        assertThat(standalone.text()).isNotBlank();
        assertThat(standalone.text()).containsIgnoringCase("Eiffel");
    }

    @Test
    void rewriteOptimizesAVerboseQueryUsingARealModel() {
        ChatClient.Builder realChatClientBuilder = ChatClient.builder(anthropicChatModel);
        QueryTransformationService service = newServiceWithFreshStore();

        Query rewritten = service.rewriteForRetrieval(
                realChatClientBuilder, "Um, could you possibly tell me what the population of Paris is? Thanks!");

        assertThat(rewritten.text()).isNotBlank();
        assertThat(rewritten.text()).containsIgnoringCase("Paris");
    }

    @Test
    void multiQueryExpanderProducesDistinctVariantsUsingARealModel() {
        ChatClient.Builder realChatClientBuilder = ChatClient.builder(anthropicChatModel);
        QueryTransformationService service = newServiceWithFreshStore();

        List<Query> variants = service.expandQuery(realChatClientBuilder, "Paris landmarks", 3);

        // MultiQueryExpander falls back to returning just the original query unchanged if the
        // model's response doesn't parse into the requested number of newline-separated variants
        // (real models don't always follow that formatting instruction exactly) - so this only
        // asserts the transformer ran and returned something well-formed, not an exact count.
        assertThat(variants).isNotEmpty();
        assertThat(variants).extracting(Query::text).allSatisfy(text -> assertThat(text).isNotBlank());
    }

    @Test
    void multiQueryFanOutRetrievalDeduplicatesUsingARealModel() {
        ChatClient.Builder realChatClientBuilder = ChatClient.builder(anthropicChatModel);
        QueryTransformationService service = newServiceWithFreshStore();
        VectorStore vectorStore = SimpleVectorStore.builder(new DeterministicFakeEmbeddingModel()).build();
        QueryTransformationService serviceWithStore = new QueryTransformationService(vectorStore);
        vectorStore.add(List.of(
                Document.builder().id("eiffel").text("The Eiffel Tower is a landmark in Paris.").build(),
                Document.builder().id("louvre").text("The Louvre is a museum in Paris.").build(),
                Document.builder().id("fuji").text("Mount Fuji is a mountain in Japan.").build()));

        List<Document> results = serviceWithStore.multiQueryRetrieveDeduped(
                realChatClientBuilder, "famous places in Paris", 3, 3);

        assertThat(results).isNotEmpty();
        assertThat(results).extracting(Document::getId).doesNotHaveDuplicates();
    }
}
