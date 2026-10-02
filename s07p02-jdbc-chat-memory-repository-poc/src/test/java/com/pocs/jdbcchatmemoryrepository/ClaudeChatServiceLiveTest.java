package com.pocs.jdbcchatmemoryrepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("claude")
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class ClaudeChatServiceLiveTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatMessageRepository repository;

    @Test
    void testChatMemoryWithClaude() {
        String sessionId = "test-claude-" + System.currentTimeMillis();

        String response1 = chatService.chat(sessionId, "Hi, my favorite color is blue.");
        assertThat(response1).isNotBlank();

        String response2 = chatService.chat(sessionId, "What is my favorite color?");
        assertThat(response2.toLowerCase()).contains("blue");
        
        assertThat(repository.findByConversationIdOrderByCreatedAtAsc(sessionId)).isNotEmpty();
    }
}
