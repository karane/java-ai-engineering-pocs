package com.pocs.chatclientfluent;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ChatClientFluentApiApplicationTests {

    @Autowired
    @Qualifier("defaultChatClient")
    private ChatClient defaultChatClient;

    @Autowired
    @Qualifier("manualChatClient")
    private ChatClient manualChatClient;

    @Autowired
    private FluentChatService fluentChatService;

    @Test
    void contextLoadsAndWiresBothChatClients() {
        assertThat(defaultChatClient).isNotNull();
        assertThat(manualChatClient).isNotNull();
        assertThat(fluentChatService).isNotNull();
    }
}
