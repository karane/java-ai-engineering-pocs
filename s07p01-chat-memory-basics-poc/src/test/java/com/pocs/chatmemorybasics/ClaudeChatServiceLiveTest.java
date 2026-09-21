package com.pocs.chatmemorybasics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("claude")
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class ClaudeChatServiceLiveTest {

    @Autowired
    private ChatService chatService;

    @Test
    void testChatMemoryWithClaude() {
        String conversationId = "test-claude-" + System.currentTimeMillis();

        String response1 = chatService.chat(conversationId, "Hello, my name is Charlie.");
        assertThat(response1).isNotBlank();

        String response2 = chatService.chat(conversationId, "What is my name?");
        assertThat(response2).containsIgnoringCase("Charlie");
    }
}
