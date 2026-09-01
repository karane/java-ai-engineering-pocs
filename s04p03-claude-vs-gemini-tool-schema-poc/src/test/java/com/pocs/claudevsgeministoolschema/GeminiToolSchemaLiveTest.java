package com.pocs.claudevsgeministoolschema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class GeminiToolSchemaLiveTest {

    @Autowired
    private ToolSchemaService toolSchemaService;

    @Test
    void restrictedToolNamesStillLetsGeminiChooseToCallGetWeather() {
        List<AssistantMessage.ToolCall> toolCalls = toolSchemaService.geminiRestrictedToolNames(
                "What's the weather like in Paris right now?");

        assertThat(toolCalls).isNotEmpty();
        assertThat(toolCalls.get(0).name()).isEqualTo("get_weather");
    }

    @Test
    void parallelToolCallCanIssueMultipleCallsInOneTurn() {
        List<AssistantMessage.ToolCall> toolCalls = toolSchemaService.geminiParallelToolCall(
                "What's the weather like in Paris and in Tokyo right now?");

        assertThat(toolCalls).isNotEmpty();
        assertThat(toolCalls).allMatch(call -> call.name().equals("get_weather"));
    }
}
