package com.pocs.chatmemorybasics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ChatDemoRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ChatDemoRunner.class);

    private final ChatService chatService;

    @Value("${app.provider:claude}")
    private String provider;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${spring.ai.google.genai.api-key:demo-placeholder-not-a-real-key}")
    private String geminiApiKey;

    public ChatDemoRunner(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void run(String... args) {
        if ("claude".equals(provider) && (anthropicApiKey == null || anthropicApiKey.isBlank())) {
            logger.warn("[s07p01] Anthropic API key not set. Skipping live demo.");
            return;
        }

        if ("gemini".equals(provider) && "demo-placeholder-not-a-real-key".equals(geminiApiKey)) {
            logger.warn("[s07p01] Gemini API key not set. Skipping live demo.");
            return;
        }

        logger.info("[s07p01] Starting chat memory demo using provider: {}", provider);
        
        String conversationId = "demo-convo-" + System.currentTimeMillis();
        
        String response1 = chatService.chat(conversationId, "Hello, my name is Alice.");
        logger.info("[s07p01] Alice: Hello, my name is Alice.");
        logger.info("[s07p01] Assistant: {}", response1);
        
        String response2 = chatService.chat(conversationId, "What is my name?");
        logger.info("[s07p01] Alice: What is my name?");
        logger.info("[s07p01] Assistant: {}", response2);
    }
}
