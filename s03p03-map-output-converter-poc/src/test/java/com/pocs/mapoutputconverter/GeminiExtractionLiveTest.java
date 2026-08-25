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
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class GeminiExtractionLiveTest {

    @Autowired
    @Qualifier("geminiChatClient")
    private ChatClient geminiChatClient;

    @Autowired
    private ExtractionService extractionService;

    @Test
    void extractsKeyValuePairsFromGemini() {
        Map<String, Object> fields = extractionService.extractKeyValuePairs(geminiChatClient,
                "Order #4521 was placed by Priya Nair for 3 units, total $214.50.");

        assertThat(fields).isNotEmpty();
    }

    @Test
    void extractsListFromGemini() {
        List<String> items = extractionService.extractList(geminiChatClient, "hiking boot brands");

        assertThat(items).isNotEmpty();
    }
}
