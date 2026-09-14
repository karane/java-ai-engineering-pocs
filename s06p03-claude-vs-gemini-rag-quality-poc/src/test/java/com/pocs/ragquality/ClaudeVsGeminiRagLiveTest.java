package com.pocs.ragquality;

import java.util.List;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import com.pocs.ragquality.GoldenDataset.QaCase;
import com.pocs.ragquality.GroundednessScorer.Score;

import static org.assertj.core.api.Assertions.assertThat;

/** Runs the full golden dataset through BOTH real, live-API-backed RAG pipelines - identical {@code QuestionAnswerAdvisor} + {@code SimpleVectorStore} retrieval, only the {@code ChatModel} differs (see {@link ChatClientConfig}) - and asserts each provider clears a groundedness bar. */
@SpringBootTest
class ClaudeVsGeminiRagLiveTest {

    @Autowired
    @Qualifier("anthropicRagChatClient")
    private ChatClient anthropicRagChatClient;

    @Autowired
    @Qualifier("geminiRagChatClient")
    private ChatClient geminiRagChatClient;

    private static List<QaCase> goldenCases() {
        return GoldenDataset.CASES;
    }

    @ParameterizedTest(name = "[Claude] {0}")
    @MethodSource("goldenCases")
    @EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
    void claudeAnswerIsGroundedInRetrievedContext(QaCase qaCase) {
        assertGrounded(anthropicRagChatClient, qaCase);
    }

    @ParameterizedTest(name = "[Gemini] {0}")
    @MethodSource("goldenCases")
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    void geminiAnswerIsGroundedInRetrievedContext(QaCase qaCase) {
        assertGrounded(geminiRagChatClient, qaCase);
    }

    private void assertGrounded(ChatClient chatClient, QaCase qaCase) {
        String answer = new RagService(chatClient).ask(qaCase.question());
        Score score = GroundednessScorer.score(qaCase, answer);

        // A real model paraphrases, so we don't require every fact verbatim (unlike the stub harness) -
        // just that most of the expected facts made it into the grounded answer.
        assertThat(score.groundedness())
                .as("question '%s' answer:%n%s%nmissing facts: %s",
                        qaCase.question(), answer, score.missingFacts())
                .isGreaterThanOrEqualTo(0.5);
    }
}
