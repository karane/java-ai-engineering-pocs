package com.pocs.chatmemorybasics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("gemini")
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class GeminiChatServiceLiveTest {

    @Autowired
    private ChatService chatService;

    @Test
    void testChatMemoryWithGemini() {
        String conversationId = "test-gemini-" + System.currentTimeMillis();

        String response1 = chatService.chat(conversationId, "Hello, my name is Delta.");
        assertThat(response1).isNotBlank();

        String response2 = chatService.chat(conversationId, "What is my name?");
        assertThat(response2).containsIgnoringCase("Delta");
    }
}
