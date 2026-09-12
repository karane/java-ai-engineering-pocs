package com.pocs.retrievalaugmentationadvisor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RetrievalAugmentationAdvisorDemoRunner implements CommandLineRunner {

    @Autowired
    private RetrievalAugmentationService retrievalAugmentationService;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void run(String... args) throws Exception {
        String geminiKey = System.getenv("GEMINI_API_KEY");
        String anthropicKey = System.getenv("ANTHROPIC_API_KEY");
        if (!StringUtils.hasText(geminiKey) || !StringUtils.hasText(anthropicKey)) {
            System.out.println("[s06p02] GEMINI_API_KEY and/or ANTHROPIC_API_KEY not set - skipping live demo call. "
                    + "Export both and re-run to see a real RetrievalAugmentationAdvisor-grounded chat response.");
            return;
        }
        try {
            retrievalAugmentationService.loadProductDocs(KnowledgeBase.productDocs());
            retrievalAugmentationService.loadSupportDocs(KnowledgeBase.supportDocs());

            System.out.println("[s06p02] --- Strict (default ContextualQueryAugmenter, in-scope question) ---");
            System.out.println("[s06p02] "
                    + retrievalAugmentationService.askStrict("What does RetrievalAugmentationAdvisor do?"));

            System.out.println("[s06p02] --- Strict vs permissive on an OUT-OF-SCOPE question ---");
            String outOfScopeQuestion = "What is the boiling point of tungsten?";
            System.out.println("[s06p02] strict:     "
                    + retrievalAugmentationService.askStrict(outOfScopeQuestion));
            System.out.println("[s06p02] permissive: "
                    + retrievalAugmentationService.askPermissive(outOfScopeQuestion));

            System.out.println("[s06p02] --- Multi-source (DocumentJoiner merging two vector stores) ---");
            System.out.println("[s06p02] "
                    + retrievalAugmentationService.askMultiSource("What is RAG and how does query augmentation work?"));
        } catch (RuntimeException e) {
            System.out.println("[s06p02] Demo call failed, but the app started fine: " + e.getMessage());
        }
        // RetrievalAugmentationAdvisor leaves non-daemon "ai-advisor-*" threads running, which
        // would otherwise keep the JVM alive forever after this one-shot demo finishes.
        System.exit(org.springframework.boot.SpringApplication.exit(applicationContext));
    }
}
