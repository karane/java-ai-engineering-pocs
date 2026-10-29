package com.pocs.llmasjudgeevaluation;

import java.util.ArrayList;
import java.util.List;

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
 * Same CI-gated threshold as {@link AnthropicJudgeLiveTest}, run against a real Gemini judge.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class GeminiJudgeLiveTest {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    @Qualifier("googleGenAiChatModel")
    private ChatModel geminiChatModel;

    @Test
    void geminiJudgeGateForFactCheckingPassesOnTheGoldenDataset() {
        LlmJudgeEvaluationService evaluationService = new LlmJudgeEvaluationService(ChatClient.builder(geminiChatModel));

        List<EvaluationResponse> relevancyResponses = new ArrayList<>();
        List<EvaluationResponse> factCheckingResponses = new ArrayList<>();
        for (GoldenCase goldenCase : GoldenDataset.CASES) {
            RagAnswerService ragAnswerService =
                    new RagAnswerService(new CannedChatModel(goldenCase.groundedAnswer()), vectorStore);
            RagAnswer ragAnswer = ragAnswerService.answer(goldenCase.question());
            relevancyResponses.add(evaluationService.evaluateRelevancy(ragAnswer));
            factCheckingResponses.add(evaluationService.evaluateFactChecking(ragAnswer));
        }

        QualityGate relevancyGate = QualityGate.evaluate(relevancyResponses);
        QualityGate factCheckingGate = QualityGate.evaluate(factCheckingResponses);

        assertThat(relevancyGate.passes()).as(relevancyGate::toString).isTrue();
        assertThat(factCheckingGate.passes()).as(factCheckingGate::toString).isTrue();
    }

    @Test
    void geminiJudgeFailsAnUngroundedAnswer() {
        LlmJudgeEvaluationService evaluationService = new LlmJudgeEvaluationService(ChatClient.builder(geminiChatModel));
        RagAnswerService ragAnswerService =
                new RagAnswerService(new CannedChatModel(GoldenDataset.UNGROUNDED_ANSWER), vectorStore);

        RagAnswer ragAnswer = ragAnswerService.answer(GoldenDataset.CASES.get(0).question());

        EvaluationResponse factChecking = evaluationService.evaluateFactChecking(ragAnswer);
        assertThat(factChecking.isPass()).isFalse();
    }
}
