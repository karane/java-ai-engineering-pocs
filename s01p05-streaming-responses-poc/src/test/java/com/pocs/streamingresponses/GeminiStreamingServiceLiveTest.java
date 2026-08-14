package com.pocs.streamingresponses;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class GeminiStreamingServiceLiveTest {

    private static final String TOPIC = "the first successful Moon landing";

    @Autowired
    private StreamingService streamingService;

    @Test
    void streamContentEmitsAtLeastOneChunkAndCompletes() {
        Flux<String> stream = streamingService.geminiStreamContent(TOPIC);

        StepVerifier.create(stream)
                .thenConsumeWhile(chunk -> true)
                .verifyComplete();
    }

    @Test
    void streamChatResponseCarriesFullChatResponseChunks() {
        Flux<ChatResponse> stream = streamingService.geminiStreamChatResponse(TOPIC);

        StepVerifier.create(stream)
                .thenConsumeWhile(response -> {
                    assertThat(response.getResult()).isNotNull();
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void collectStatsAssemblesFullTextAcrossMultipleChunks() {
        StreamingChunkStats stats = streamingService.collectStats(streamingService.geminiStreamContent(TOPIC));

        assertThat(stats.fullText()).isNotBlank();
        assertThat(stats.chunkCount()).isGreaterThan(0);
    }
}
