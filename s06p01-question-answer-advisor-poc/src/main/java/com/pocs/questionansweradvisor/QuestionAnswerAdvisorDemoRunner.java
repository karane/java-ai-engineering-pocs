package com.pocs.questionansweradvisor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class QuestionAnswerAdvisorDemoRunner implements CommandLineRunner {

    @Autowired
    private QuestionAnsweringService questionAnsweringService;

    @Override
    public void run(String... args) throws Exception {
        String geminiKey = System.getenv("GEMINI_API_KEY");
        String anthropicKey = System.getenv("ANTHROPIC_API_KEY");
        if (!StringUtils.hasText(geminiKey) || !StringUtils.hasText(anthropicKey)) {
            System.out.println("[s06p01] GEMINI_API_KEY and/or ANTHROPIC_API_KEY not set - skipping live demo call. "
                    + "Export both and re-run to see a real QuestionAnswerAdvisor-grounded chat response.");
            return;
        }
        try {
            questionAnsweringService.loadKnowledgeBase(KnowledgeBase.sampleDocuments());

            System.out.println("[s06p01] --- Default QuestionAnswerAdvisor ---");
            String answer = questionAnsweringService.ask("What is QuestionAnswerAdvisor and what does it do?");
            System.out.println("[s06p01] " + answer);

            System.out.println("[s06p01] --- Per-query SearchRequest (topK=1, high similarityThreshold) ---");
            String narrowAnswer = questionAnsweringService.askWithSearchRequest(
                    "What is QuestionAnswerAdvisor and what does it do?", 1, 0.5);
            System.out.println("[s06p01] " + narrowAnswer);

            System.out.println("[s06p01] --- Scoped to topic=geography (should NOT mention Spring AI) ---");
            String scopedAnswer = questionAnsweringService.askScopedToTopic(
                    "What is QuestionAnswerAdvisor and what does it do?", "geography");
            System.out.println("[s06p01] " + scopedAnswer);
        } catch (RuntimeException e) {
            System.out.println("[s06p01] Demo call failed, but the app started fine: " + e.getMessage());
        }
    }
}
