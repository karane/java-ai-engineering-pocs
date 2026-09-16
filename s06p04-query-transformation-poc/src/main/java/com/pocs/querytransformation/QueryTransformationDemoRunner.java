package com.pocs.querytransformation;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Runs each transformer with the fake, no-key {@link ChatClient.Builder} bean (the autoconfigured default, backed by {@link FakeTransformationChatModel} per {@link QueryTransformationConfig}) so the demo works without ANTHROPIC_API_KEY. */
@Component
public class QueryTransformationDemoRunner implements CommandLineRunner {

    @Autowired
    private QueryTransformationService queryTransformationService;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private org.springframework.ai.vectorstore.VectorStore vectorStore;

    @Override
    public void run(String... args) {
        vectorStore.add(List.of(
                Document.builder().text("The Eiffel Tower is a wrought-iron lattice tower in Paris, France.")
                        .metadata("topic", "landmarks").build(),
                Document.builder().text("Paris is the capital and most populous city of France.")
                        .metadata("topic", "geography").build(),
                Document.builder().text("The Louvre in Paris is the world's most visited art museum.")
                        .metadata("topic", "landmarks").build(),
                Document.builder().text("Mount Fuji is the tallest mountain in Japan.")
                        .metadata("topic", "geography").build()));

        System.out.println("[s06p04] --- CompressionQueryTransformer ---");
        List<Message> history = List.of(
                new UserMessage("Tell me about the Eiffel Tower."),
                new AssistantMessage("The Eiffel Tower is a wrought-iron lattice tower in Paris, France."));
        Query standalone = queryTransformationService.compressConversation(
                chatClientBuilder, history, "How tall is it?");
        System.out.println("[s06p04] Standalone query: " + standalone.text());

        System.out.println("[s06p04] --- RewriteQueryTransformer ---");
        Query rewritten = queryTransformationService.rewriteForRetrieval(
                chatClientBuilder, "Um, can you please tell me about the population of Paris?");
        System.out.println("[s06p04] Rewritten query: " + rewritten.text());

        System.out.println("[s06p04] --- MultiQueryExpander ---");
        List<Query> variants = queryTransformationService.expandQuery(chatClientBuilder, "Paris landmarks", 3);
        variants.forEach(v -> System.out.println("[s06p04]  variant: " + v.text()));

        System.out.println("[s06p04] --- Multi-query fan-out retrieval, deduplicated ---");
        List<Document> results = queryTransformationService.multiQueryRetrieveDeduped(
                chatClientBuilder, "famous things to see in Paris", 3, 2);
        System.out.println("[s06p04] " + results.size() + " unique documents after dedup:");
        results.forEach(d -> System.out.println("[s06p04]  - " + d.getText()));
    }
}
