package com.pocs.springaigemini;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpringAiGeminiSetupApplicationTests {

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private GeminiService geminiService;

    @Test
    void contextLoadsAndWiresGeminiChatModel() {
        assertThat(chatModel).isNotNull();
        assertThat(geminiService).isNotNull();
    }
}
