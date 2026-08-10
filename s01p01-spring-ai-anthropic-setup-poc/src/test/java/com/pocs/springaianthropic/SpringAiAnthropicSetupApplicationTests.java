package com.pocs.springaianthropic;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpringAiAnthropicSetupApplicationTests {

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private ClaudeService claudeService;

    @Test
    void contextLoadsAndWiresAnthropicChatModel() {
        assertThat(chatModel).isNotNull();
        assertThat(claudeService).isNotNull();
    }
}
