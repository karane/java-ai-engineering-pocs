package com.pocs.ragquality;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;

import com.pocs.ragquality.GoldenDataset.QaCase;
import com.pocs.ragquality.GroundednessScorer.Score;

import static org.assertj.core.api.Assertions.assertThat;

/** Proves the golden-dataset harness's plumbing and scoring logic without any API key: builds the exact same {@code SimpleVectorStore} + {@code QuestionAnswerAdvisor} + {@code ChatClient} pipeline the real Claude/Gemini beans use in {@link ChatClientConfig}, but backed by {@link StubEchoChatModel} instead of a real LLM. */
class GoldenDatasetHarnessTest {

    private static ChatClient buildStubRagChatClient() {
        VectorStore vectorStore = SimpleVectorStore.builder(new DeterministicFakeEmbeddingModel()).build();
        vectorStore.add(new KnowledgeBaseConfig().knowledgeBaseDocuments());
        return ChatClient.builder(new StubEchoChatModel())
                .defaultAdvisors(ChatClientConfig.ragAdvisor(vectorStore))
                .build();
    }

    @Test
    void everyGoldenCaseRetrievesItsExpectedFactsThroughTheRealPipeline() {
        ChatClient stubRagChatClient = buildStubRagChatClient();
        RagService ragService = new RagService(stubRagChatClient);

        List<Score> scores = GoldenDataset.CASES.stream()
                .map(qaCase -> GroundednessScorer.score(qaCase, ragService.ask(qaCase.question())))
                .toList();

        for (Score score : scores) {
            assertThat(score.isFullyGrounded())
                    .as("case '%s' missing facts %s in echoed answer:%n%s",
                            score.qaCase().question(), score.missingFacts(), score.answer())
                    .isTrue();
        }

        double averageGroundedness = scores.stream().mapToDouble(Score::groundedness).average().orElseThrow();
        assertThat(averageGroundedness).isEqualTo(1.0);
    }

    @Test
    void scorerFlagsFactsThatAreGenuinelyMissingFromAnAnswer() {
        QaCase qaCase = new QaCase("irrelevant", List.of("Priya Malhotra", "2019", "Berlin"));

        Score score = GroundednessScorer.score(qaCase, "Aurora Notes was founded by Priya Malhotra in 2019.");

        assertThat(score.matchedFacts()).containsExactlyInAnyOrder("Priya Malhotra", "2019");
        assertThat(score.missingFacts()).containsExactly("Berlin");
        assertThat(score.isFullyGrounded()).isFalse();
        assertThat(score.groundedness()).isEqualTo(2.0 / 3.0);
    }
}
