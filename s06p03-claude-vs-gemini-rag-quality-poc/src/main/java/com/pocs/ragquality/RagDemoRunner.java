package com.pocs.ragquality;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Prints the same question run through both RAG pipelines when a key is available for that provider, so
 * `mvn spring-boot:run` gives a quick side-by-side look at groundedness/citation-style differences without
 * needing the JUnit harness. Silently skips a provider whose API key env var isn't set.
 */
@Component
public class RagDemoRunner implements CommandLineRunner {

    private static final String DEMO_QUESTION =
            "How much does Aurora Notes Pro cost, and is encryption included with the free plan?";

    private final ChatClient anthropicRagChatClient;
    private final ChatClient geminiRagChatClient;

    public RagDemoRunner(ChatClient anthropicRagChatClient, ChatClient geminiRagChatClient) {
        this.anthropicRagChatClient = anthropicRagChatClient;
        this.geminiRagChatClient = geminiRagChatClient;
    }

    @Override
    public void run(String... args) {
        System.out.println("Question: " + DEMO_QUESTION);

        if (isSet("ANTHROPIC_API_KEY")) {
            System.out.println("\n--- Claude (Anthropic) ---");
            System.out.println(new RagService(anthropicRagChatClient).ask(DEMO_QUESTION));
        } else {
            System.out.println("\n--- Claude (Anthropic) --- skipped, ANTHROPIC_API_KEY not set");
        }

        if (isSet("GEMINI_API_KEY")) {
            System.out.println("\n--- Gemini ---");
            System.out.println(new RagService(geminiRagChatClient).ask(DEMO_QUESTION));
        } else {
            System.out.println("\n--- Gemini --- skipped, GEMINI_API_KEY not set");
        }
    }

    private boolean isSet(String envVar) {
        String value = System.getenv(envVar);
        return value != null && !value.isBlank();
    }
}
