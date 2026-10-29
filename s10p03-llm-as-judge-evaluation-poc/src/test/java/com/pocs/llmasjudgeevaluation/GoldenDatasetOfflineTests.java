package com.pocs.llmasjudgeevaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;

import static org.assertj.core.api.Assertions.assertThat;

/** Fully offline, parameterized golden-dataset tests using {@link CannedChatModel} as answerer and judge. */
class GoldenDatasetOfflineTests {

    private static VectorStore newKnowledgeBaseVectorStore() {
        VectorStore vectorStore = SimpleVectorStore.builder(new DeterministicFakeEmbeddingModel()).build();
        vectorStore.add(KnowledgeBaseConfig.KNOWLEDGE_BASE);
        return vectorStore;
    }

    private static Stream<GoldenCase> goldenCases() {
        return GoldenDataset.CASES.stream();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("goldenCases")
    void groundedAnswerPassesRelevancyAndFactCheckingAgainstAFakePassJudge(GoldenCase goldenCase) {
        RagAnswerService ragAnswerService =
                new RagAnswerService(new CannedChatModel(goldenCase.groundedAnswer()), newKnowledgeBaseVectorStore());
        LlmJudgeEvaluationService evaluationService =
                new LlmJudgeEvaluationService(ChatClient.builder(new CannedChatModel("YES")));

        RagAnswer ragAnswer = ragAnswerService.answer(goldenCase.question());
        assertThat(ragAnswer.retrievedDocuments()).isNotEmpty();

        EvaluationResponse relevancy = evaluationService.evaluateRelevancy(ragAnswer);
        EvaluationResponse factChecking = evaluationService.evaluateFactChecking(ragAnswer);

        assertThat(relevancy.isPass()).isTrue();
        assertThat(factChecking.isPass()).isTrue();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("goldenCases")
    void groundedAnswerPassesTheConcisenessEvaluator(GoldenCase goldenCase) {
        ConcisenessEvaluator concisenessEvaluator = new ConcisenessEvaluator(40);

        EvaluationResponse response = concisenessEvaluator.evaluate(
                new EvaluationRequest(goldenCase.question(), List.of(), goldenCase.groundedAnswer()));

        assertThat(response.isPass()).isTrue();
    }

    @Test
    void fakeFailJudgeFlipsLlmEvaluatorsToFailButConcisenessIsUnaffected() {
        GoldenCase goldenCase = GoldenDataset.CASES.get(0);
        RagAnswerService ragAnswerService = new RagAnswerService(
                new CannedChatModel(GoldenDataset.UNGROUNDED_ANSWER), newKnowledgeBaseVectorStore());
        LlmJudgeEvaluationService evaluationService =
                new LlmJudgeEvaluationService(ChatClient.builder(new CannedChatModel("NO")));

        RagAnswer ragAnswer = ragAnswerService.answer(goldenCase.question());

        EvaluationResponse relevancy = evaluationService.evaluateRelevancy(ragAnswer);
        EvaluationResponse factChecking = evaluationService.evaluateFactChecking(ragAnswer);
        assertThat(relevancy.isPass()).isFalse();
        assertThat(factChecking.isPass()).isFalse();

        // The ungrounded answer is still short, so the length-only conciseness check still passes -
        // proving the two kinds of evaluator are independent dimensions, not redundant with each other.
        ConcisenessEvaluator concisenessEvaluator = new ConcisenessEvaluator(40);
        EvaluationResponse conciseness = concisenessEvaluator.evaluate(
                new EvaluationRequest(goldenCase.question(), List.of(), GoldenDataset.UNGROUNDED_ANSWER));
        assertThat(conciseness.isPass()).isTrue();
    }

    @Test
    void qualityGatePassesWhenEveryGoldenCaseIsGroundedAgainstAFakePassJudge() {
        LlmJudgeEvaluationService evaluationService =
                new LlmJudgeEvaluationService(ChatClient.builder(new CannedChatModel("YES")));
        VectorStore vectorStore = newKnowledgeBaseVectorStore();

        List<EvaluationResponse> factCheckingResponses = new ArrayList<>();
        for (GoldenCase goldenCase : GoldenDataset.CASES) {
            RagAnswerService ragAnswerService =
                    new RagAnswerService(new CannedChatModel(goldenCase.groundedAnswer()), vectorStore);
            RagAnswer ragAnswer = ragAnswerService.answer(goldenCase.question());
            factCheckingResponses.add(evaluationService.evaluateFactChecking(ragAnswer));
        }

        QualityGate gate = QualityGate.evaluate(factCheckingResponses);
        assertThat(gate.passes()).as(gate::toString).isTrue();
        assertThat(gate.passRate()).isEqualTo(1.0);
    }
}
