package com.pocs.mapoutputconverter;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Exercises MapOutputConverter/ListOutputConverter parsing plus the markdown-fence
 * stripping ExtractionService applies for Gemini-style output, with the ChatClient
 * mocked out entirely — no live call, no key required.
 */
class ExtractionServiceConverterUnitTest {

    private final ExtractionService extractionService = new ExtractionService();

    @Test
    void extractKeyValuePairsParsesBareJson() {
        ChatClient chatClient = chatClientReturning("{\"order_id\": \"4521\", \"customer\": \"Priya Nair\"}");

        Map<String, Object> result = extractionService.extractKeyValuePairs(chatClient, "irrelevant");

        assertThat(result).containsEntry("order_id", "4521").containsEntry("customer", "Priya Nair");
    }

    @Test
    void extractKeyValuePairsStripsMarkdownFenceBeforeParsing() {
        ChatClient chatClient = chatClientReturning("```json\n{\"city\": \"Bengaluru\"}\n```");

        Map<String, Object> result = extractionService.extractKeyValuePairs(chatClient, "irrelevant");

        assertThat(result).containsEntry("city", "Bengaluru");
    }

    @Test
    void extractListParsesCommaDelimitedOutput() {
        ChatClient chatClient = chatClientReturning("Salomon, Merrell, Vasque, Keen, Lowa");

        List<String> result = extractionService.extractList(chatClient, "hiking boot brands");

        assertThat(result).containsExactly("Salomon", "Merrell", "Vasque", "Keen", "Lowa");
    }

    @Test
    void extractListStripsMarkdownFenceBeforeParsing() {
        ChatClient chatClient = chatClientReturning("```\nSalomon, Merrell, Vasque, Keen, Lowa\n```");

        List<String> result = extractionService.extractList(chatClient, "hiking boot brands");

        assertThat(result).containsExactly("Salomon", "Merrell", "Vasque", "Keen", "Lowa");
    }

    private ChatClient chatClientReturning(String content) {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClientRequestSpec requestSpec = mock(ChatClientRequestSpec.class);
        CallResponseSpec callResponseSpec = mock(CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(org.mockito.ArgumentMatchers.anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(content);

        return chatClient;
    }
}
