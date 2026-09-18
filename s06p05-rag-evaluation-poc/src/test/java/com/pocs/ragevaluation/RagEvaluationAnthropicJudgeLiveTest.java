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

/** Regression test asserting evaluator score thresholds using a real Anthropic Claude model as the LLM judge — the "per provider" half of this POC alongside {@link RagEvaluationGeminiJudgeLiveTest}. */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class RagEvaluationAnthropicJudgeLiveTest {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    @Qualifier("anthropicChatModel")
    private ChatModel anthropicChatModel;

    @Test
    void claudeJudgePassesAGroundedAnswerOnEveryGoldenCase() {
        RagEvaluationService evaluationService = new RagEvaluationService(ChatClient.builder(anthropicChatModel));

        for (GoldenCase goldenCase : GoldenDataset.CASES) {
            RagAnswerService ragAnswerService =
                    new RagAnswerService(new CannedChatModel(goldenCase.groundedAnswer()), vectorStore);
            RagAnswer ragAnswer = ragAnswerService.answer(goldenCase.question());

            EvaluationResponse relevancy = evaluationService.evaluateRelevancy(ragAnswer);
            EvaluationResponse factChecking = evaluationService.evaluateFactChecking(ragAnswer);

            assertThat(relevancy.isPass())
                    .as("Claude relevancy verdict for: %s", goldenCase.question())
                    .isTrue();
            assertThat(factChecking.isPass())
                    .as("Claude fact-checking verdict for: %s", goldenCase.question())
                    .isTrue();
        }
    }

    @Test
    void claudeJudgeFailsAnUngroundedAnswer() {
        RagEvaluationService evaluationService = new RagEvaluationService(ChatClient.builder(anthropicChatModel));
        RagAnswerService ragAnswerService =
                new RagAnswerService(new CannedChatModel(GoldenDataset.UNGROUNDED_ANSWER), vectorStore);

        RagAnswer ragAnswer = ragAnswerService.answer(GoldenDataset.CASES.get(0).question());

        EvaluationResponse factChecking = evaluationService.evaluateFactChecking(ragAnswer);
        assertThat(factChecking.isPass()).isFalse();
    }
}
