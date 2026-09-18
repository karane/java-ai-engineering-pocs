package com.pocs.ragevaluation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Same regression test as {@link RagEvaluationAnthropicJudgeLiveTest}, but with a real
 * Gemini model as the LLM judge — the other half of "per provider" evaluator coverage.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class RagEvaluationGeminiJudgeLiveTest {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    @Qualifier("googleGenAiChatModel")
    private ChatModel googleGenAiChatModel;

    @Test
    void geminiJudgePassesAGroundedAnswerOnEveryGoldenCase() {
        RagEvaluationService evaluationService = new RagEvaluationService(ChatClient.builder(googleGenAiChatModel));

        for (GoldenCase goldenCase : GoldenDataset.CASES) {
            RagAnswerService ragAnswerService =
                    new RagAnswerService(new CannedChatModel(goldenCase.groundedAnswer()), vectorStore);
            RagAnswer ragAnswer = ragAnswerService.answer(goldenCase.question());

            EvaluationResponse relevancy = evaluationService.evaluateRelevancy(ragAnswer);
            EvaluationResponse factChecking = evaluationService.evaluateFactChecking(ragAnswer);

            assertThat(relevancy.isPass())
                    .as("Gemini relevancy verdict for: %s", goldenCase.question())
                    .isTrue();
            assertThat(factChecking.isPass())
                    .as("Gemini fact-checking verdict for: %s", goldenCase.question())
                    .isTrue();
        }
    }

    @Test
    void geminiJudgeFailsAnUngroundedAnswer() {
        RagEvaluationService evaluationService = new RagEvaluationService(ChatClient.builder(googleGenAiChatModel));
        RagAnswerService ragAnswerService =
                new RagAnswerService(new CannedChatModel(GoldenDataset.UNGROUNDED_ANSWER), vectorStore);

        RagAnswer ragAnswer = ragAnswerService.answer(GoldenDataset.CASES.get(0).question());

        EvaluationResponse factChecking = evaluationService.evaluateFactChecking(ragAnswer);
        assertThat(factChecking.isPass()).isFalse();
    }
}
