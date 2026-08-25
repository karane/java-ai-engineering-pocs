package com.pocs.mapoutputconverter;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class ClaudeExtractionLiveTest {

    @Autowired
    @Qualifier("anthropicChatClient")
    private ChatClient anthropicChatClient;

    @Autowired
    private ExtractionService extractionService;

    @Test
    void extractsKeyValuePairsFromClaude() {
        Map<String, Object> fields = extractionService.extractKeyValuePairs(anthropicChatClient,
                "Order #4521 was placed by Priya Nair for 3 units, total $214.50.");

        assertThat(fields).isNotEmpty();
    }

    @Test
    void extractsListFromClaude() {
        List<String> items = extractionService.extractList(anthropicChatClient, "hiking boot brands");

        assertThat(items).isNotEmpty();
    }
}
