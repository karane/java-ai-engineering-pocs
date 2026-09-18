package com.pocs.ragevaluation;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;

import static org.assertj.core.api.Assertions.assertThat;

/** Fully offline: a real {@link SimpleVectorStore} + {@code QuestionAnswerAdvisor} pipeline (via {@link RagAnswerService}), evaluated by the real Spring AI {@code RelevancyEvaluator}/{@code FactCheckingEvaluator}, but with a {@link CannedChatModel} standing in for both the "answerer" and the "judge" — so the whole retrieve/answer/evaluate/assert chain is proven without any API key. */
class RagEvaluationOfflineTests {

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
    void groundedAnswerPassesBothEvaluatorsAgainstAFakePassJudge(GoldenCase goldenCase) {
        RagAnswerService ragAnswerService =
                new RagAnswerService(new CannedChatModel(goldenCase.groundedAnswer()), newKnowledgeBaseVectorStore());
        RagEvaluationService evaluationService =
                new RagEvaluationService(ChatClient.builder(new CannedChatModel("YES")));

        RagAnswer ragAnswer = ragAnswerService.answer(goldenCase.question());
        assertThat(ragAnswer.retrievedDocuments()).isNotEmpty();

        EvaluationResponse relevancy = evaluationService.evaluateRelevancy(ragAnswer);
        EvaluationResponse factChecking = evaluationService.evaluateFactChecking(ragAnswer);

        assertThat(relevancy.isPass()).isTrue();
        assertThat(relevancy.getScore()).isGreaterThanOrEqualTo(1.0f);
        assertThat(factChecking.isPass()).isTrue();
    }

    @Test
    void fakeFailJudgeFlipsBothEvaluatorsToFail() {
        GoldenCase goldenCase = GoldenDataset.CASES.get(0);
        RagAnswerService ragAnswerService =
                new RagAnswerService(new CannedChatModel(GoldenDataset.UNGROUNDED_ANSWER), newKnowledgeBaseVectorStore());
        RagEvaluationService evaluationService =
                new RagEvaluationService(ChatClient.builder(new CannedChatModel("NO")));

        RagAnswer ragAnswer = ragAnswerService.answer(goldenCase.question());

        EvaluationResponse relevancy = evaluationService.evaluateRelevancy(ragAnswer);
        EvaluationResponse factChecking = evaluationService.evaluateFactChecking(ragAnswer);

        assertThat(relevancy.isPass()).isFalse();
        assertThat(relevancy.getScore()).isEqualTo(0.0f);
        assertThat(factChecking.isPass()).isFalse();
    }

    @Test
    void ragAnswerRetrievesTheWholeSmallKnowledgeBaseAsContext() {
        RagAnswerService ragAnswerService =
                new RagAnswerService(new CannedChatModel("any answer"), newKnowledgeBaseVectorStore());

        RagAnswer ragAnswer = ragAnswerService.answer(GoldenDataset.CASES.get(0).question());

        assertThat(ragAnswer.retrievedDocuments()).hasSize(1);
        assertThat(ragAnswer.content()).isEqualTo("any answer");
    }
}
