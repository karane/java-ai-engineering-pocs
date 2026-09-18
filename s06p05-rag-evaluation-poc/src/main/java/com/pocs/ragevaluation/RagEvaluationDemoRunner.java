package com.pocs.ragevaluation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Console-only demo: runs the golden questions through the RAG pipeline using a fake, fully offline "answerer" model, then judges each answer with a fake "PASS" judge — proving the whole evaluate-and-assert pipeline works with zero API keys. */
@Component
public class RagEvaluationDemoRunner implements CommandLineRunner {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    @Qualifier("anthropicChatModel")
    private org.springframework.ai.chat.model.ChatModel anthropicChatModel;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    @Override
    public void run(String... args) {
        RagAnswerService ragAnswerService = new RagAnswerService(new CannedChatModel(
                "This answer is grounded in the retrieved knowledge base context."), vectorStore);
        RagEvaluationService fakeJudgeService =
                new RagEvaluationService(ChatClient.builder(new CannedChatModel("YES")));

        System.out.println("[s06p05] Offline RAG evaluation demo (fake answerer + fake PASS judge):");
        for (GoldenCase goldenCase : GoldenDataset.CASES) {
            RagAnswer ragAnswer = ragAnswerService.answer(goldenCase.question());
            EvaluationResponse relevancy = fakeJudgeService.evaluateRelevancy(ragAnswer);
            EvaluationResponse factChecking = fakeJudgeService.evaluateFactChecking(ragAnswer);
            System.out.printf("  Q: %-45s relevancy=%s (score=%.1f) factChecking=%s%n",
                    goldenCase.question(), relevancy.isPass(), relevancy.getScore(), factChecking.isPass());
        }

        if (!StringUtils.hasText(anthropicApiKey)) {
            System.out.println("[s06p05] ANTHROPIC_API_KEY not set — skipping live judge demo call. "
                    + "Export ANTHROPIC_API_KEY and re-run to see a real Claude judge verdict.");
            return;
        }
        try {
            RagEvaluationService liveJudgeService = new RagEvaluationService(ChatClient.builder(anthropicChatModel));
            GoldenCase sample = GoldenDataset.CASES.get(0);
            RagAnswer ragAnswer = ragAnswerService.answer(sample.question());
            EvaluationResponse relevancy = liveJudgeService.evaluateRelevancy(ragAnswer);
            System.out.println("[s06p05] Live Claude judge relevancy verdict for \"" + sample.question() + "\": "
                    + "pass=" + relevancy.isPass() + " score=" + relevancy.getScore());
        } catch (RuntimeException e) {
            System.out.println("[s06p05] Live judge demo call failed, but the app started fine: " + e.getMessage());
        }
    }
}
