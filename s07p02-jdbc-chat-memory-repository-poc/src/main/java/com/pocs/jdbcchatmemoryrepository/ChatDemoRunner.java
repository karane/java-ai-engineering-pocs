package com.pocs.jdbcchatmemoryrepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ChatDemoRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ChatDemoRunner.class);
    private final ChatService chatService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicKey;

    @Value("${spring.ai.google.genai.api-key:}")
    private String geminiKey;

    public ChatDemoRunner(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void run(String... args) {
        boolean hasAnthropic = anthropicKey != null && !anthropicKey.isBlank();
        boolean hasGemini = geminiKey != null && !geminiKey.isBlank() && !geminiKey.equals("demo-placeholder-not-a-real-key");

        if (!hasAnthropic && !hasGemini) {
            log.warn("[s07p02] No API keys configured. Skipping live demo.");
            return;
        }

        log.info("[s07p02] Starting JPA Chat Memory Demo...");
        
        String conversationId = "demo-session-1"; // Or use UUID.randomUUID().toString()
        
        log.info("[s07p02] User: Hello, my name is Alice.");
        String resp1 = chatService.chat(conversationId, "Hello, my name is Alice.");
        log.info("[s07p02] Assistant: {}", resp1);

        log.info("[s07p02] User: What is my name?");
        String resp2 = chatService.chat(conversationId, "What is my name?");
        log.info("[s07p02] Assistant: {}", resp2);
        
        log.info("[s07p02] Demo complete. Check the H2 database or your logs to see the persisted messages.");
    }
}
