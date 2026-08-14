package com.pocs.streamingresponses;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class ClaudeVsGeminiChunkGranularityLiveTest {

    private static final String TOPIC = "why the sky is blue";

    @Autowired
    private StreamingService streamingService;

    @Test
    void bothProvidersStreamMoreThanOneChunkForANonTrivialResponse() {
        StreamingChunkStats claudeStats = streamingService.collectStats(streamingService.anthropicStreamContent(TOPIC));
        StreamingChunkStats geminiStats = streamingService.collectStats(streamingService.geminiStreamContent(TOPIC));

        System.out.println("[s01p05] Claude chunk count: " + claudeStats.chunkCount());
        System.out.println("[s01p05] Gemini chunk count: " + geminiStats.chunkCount());

        assertThat(claudeStats.chunkCount()).isGreaterThan(0);
        assertThat(geminiStats.chunkCount()).isGreaterThan(0);
        assertThat(claudeStats.fullText()).isNotBlank();
        assertThat(geminiStats.fullText()).isNotBlank();
    }
}
