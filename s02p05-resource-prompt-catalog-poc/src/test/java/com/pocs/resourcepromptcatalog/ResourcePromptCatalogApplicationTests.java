package com.pocs.resourcepromptcatalog;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ResourcePromptCatalogApplicationTests {

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private PromptTemplateRegistry registry;

    @Autowired
    private CatalogChatService catalogChatService;

    @Test
    void contextLoadsAndWiresAllBeans() {
        assertThat(chatModel).isNotNull();
        assertThat(chatClient).isNotNull();
        assertThat(registry).isNotNull();
        assertThat(catalogChatService).isNotNull();
    }
}
