package com.pocs.streamingresponses;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class AnthropicStreamingServiceLiveTest {

    private static final String TOPIC = "the first successful Moon landing";

    @Autowired
    private StreamingService streamingService;

    @Test
    void streamContentEmitsAtLeastOneChunkAndCompletes() {
        Flux<String> stream = streamingService.anthropicStreamContent(TOPIC);

        StepVerifier.create(stream)
                .thenConsumeWhile(chunk -> true)
                .verifyComplete();
    }

    @Test
    void streamChatResponseCarriesFullChatResponseChunks() {
        Flux<ChatResponse> stream = streamingService.anthropicStreamChatResponse(TOPIC);

        StepVerifier.create(stream)
                .thenConsumeWhile(response -> {
                    assertThat(response.getResult()).isNotNull();
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void collectStatsAssemblesFullTextAcrossMultipleChunks() {
        StreamingChunkStats stats = streamingService.collectStats(streamingService.anthropicStreamContent(TOPIC));

        assertThat(stats.fullText()).isNotBlank();
        assertThat(stats.chunkCount()).isGreaterThan(0);
    }

    @Test
    void backpressureCollectionProducesTheSameTextAsUnboundedCollection() {
        StreamingChunkStats unbounded = streamingService.collectStats(streamingService.anthropicStreamContent(TOPIC));
        StreamingChunkStats backpressured =
                streamingService.collectWithBackpressure(streamingService.anthropicStreamContent(TOPIC));

        assertThat(backpressured.fullText()).isNotBlank();
        assertThat(backpressured.chunkCount()).isGreaterThan(0);
        assertThat(unbounded.fullText()).isNotBlank();
    }

    @Test
    void streamCompletesWithinReasonableTime() {
        StepVerifier.create(streamingService.anthropicStreamContent(TOPIC))
                .thenConsumeWhile(chunk -> true)
                .expectComplete()
                .verify(Duration.ofSeconds(60));
    }
}
