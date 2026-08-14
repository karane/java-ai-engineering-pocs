package com.pocs.streamingresponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StreamingDemoRunner implements CommandLineRunner {

    @Autowired
    private StreamingService streamingService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    @Override
    public void run(String... args) {
        String topic = "the first successful Moon landing";

        if (anthropicApiKey.isBlank()) {
            System.out.println("[s01p05] ANTHROPIC_API_KEY not set — skipping Claude streaming demo.");
        } else {
            try {
                StreamingChunkStats stats = streamingService.collectStats(streamingService.anthropicStreamContent(topic));
                System.out.println("[s01p05] Claude streamed " + stats.chunkCount() + " chunks: " + stats.fullText());

                StreamingChunkStats backpressureStats =
                        streamingService.collectWithBackpressure(streamingService.anthropicStreamContent(topic));
                System.out.println("[s01p05] Claude (backpressure, 1 chunk at a time): "
                        + backpressureStats.chunkCount() + " chunks");
            } catch (RuntimeException e) {
                System.out.println("[s01p05] Claude streaming demo failed, but the app started fine: " + e.getMessage());
            }
        }

        if (geminiApiKey.isBlank()) {
            System.out.println("[s01p05] GEMINI_API_KEY not set — skipping Gemini streaming demo.");
        } else {
            try {
                StreamingChunkStats stats = streamingService.collectStats(streamingService.geminiStreamContent(topic));
                System.out.println("[s01p05] Gemini streamed " + stats.chunkCount() + " chunks: " + stats.fullText());
            } catch (RuntimeException e) {
                System.out.println("[s01p05] Gemini streaming demo failed, but the app started fine: " + e.getMessage());
            }
        }
    }
}
