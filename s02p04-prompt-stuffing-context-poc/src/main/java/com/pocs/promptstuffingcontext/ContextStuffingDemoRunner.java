package com.pocs.promptstuffingcontext;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ContextStuffingDemoRunner implements CommandLineRunner {

    private static final String SHORT_RETRIEVED_CONTEXT = """
            Spring AI's PromptTemplate renders a template string against a variable \
            map, using st4 (StringTemplate) syntax where single curly braces such as \
            {topic} mark placeholders. Templates can be built inline from a Java \
            string or loaded from a classpath Resource, keeping prompt text out of \
            application code when that's preferred.""";

    private static final String LONG_RETRIEVED_CONTEXT = """
            Retrieval-augmented generation (RAG) combines a retrieval step - typically \
            a vector similarity search over embedded document chunks - with a \
            generation step where the retrieved chunks are "stuffed" into the model's \
            prompt as context. This lets a model answer questions about content it \
            was never trained on, and lets that content be updated without \
            retraining. The retrieval step commonly returns more chunks than fit \
            comfortably in the target token budget, especially once the prompt \
            template's own boilerplate and the user's question are added on top - \
            so a pre-flight token estimate and a truncation strategy are necessary \
            parts of the pipeline, not optional extras. Token estimators like \
            JTokkit approximate a model's real tokenizer closely enough for this \
            kind of budgeting, even when the exact byte-pair encoding differs from \
            the target model's own. """.repeat(6);

    @Autowired
    private ContextStuffingService contextStuffingService;

    @Autowired
    private ChatClient chatClient;

    @Value("${spring.ai.anthropic.api-key:}")
    private String apiKey;

    @Override
    public void run(String... args) {
        System.out.println("[s02p04] Max prompt token budget: " + contextStuffingService.maxPromptTokens());

        ContextStuffingService.StuffedPrompt fitsFine = contextStuffingService.buildPrompt(
                SHORT_RETRIEVED_CONTEXT, "What placeholder syntax does PromptTemplate use?");
        System.out.println("[s02p04] Short context — estimated tokens: " + fitsFine.finalTokenCount()
                + " (truncated: " + fitsFine.truncated() + ")");

        ContextStuffingService.StuffedPrompt truncatedCase = contextStuffingService.buildPrompt(
                LONG_RETRIEVED_CONTEXT, "Why is a truncation strategy necessary in a RAG pipeline?");
        System.out.println("[s02p04] Long context — original estimated tokens: " + truncatedCase.originalTokenCount()
                + ", after truncation: " + truncatedCase.finalTokenCount()
                + " (truncated: " + truncatedCase.truncated() + ")");

        if (apiKey.isBlank()) {
            System.out.println("[s02p04] ANTHROPIC_API_KEY not set — skipping live demo call. "
                    + "Export ANTHROPIC_API_KEY and re-run to see a real Claude response.");
            return;
        }
        
        try {
            String answer = chatClient.prompt().user(fitsFine.renderedPrompt()).call().content();
            System.out.println("[s02p04] Claude says: " + answer);
        } catch (RuntimeException e) {
            System.out.println("[s02p04] Demo call to Claude failed, but the app started fine: " + e.getMessage());
        }
    }
}
