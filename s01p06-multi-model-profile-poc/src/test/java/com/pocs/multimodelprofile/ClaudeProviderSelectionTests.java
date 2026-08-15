package com.pocs.multimodelprofile;

import org.junit.jupiter.api.Test;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("claude")
class ClaudeProviderSelectionTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ChatClient chatClient;

    @Test
    void bothConcreteChatModelsExistRegardlessOfProfile() {
        assertThat(context.getBean(AnthropicChatModel.class)).isNotNull();
        assertThat(context.getBean(GoogleGenAiChatModel.class)).isNotNull();
    }

    @Test
    void onlyOneChatClientBeanExists() {
        assertThat(context.getBeanNamesForType(ChatClient.class)).containsExactly("chatClient");
        assertThat(chatClient).isNotNull();
    }

    @Test
    void chatServiceIsWiredWithNoAmbiguity() {
        assertThat(context.getBean(ChatService.class)).isNotNull();
    }
}
