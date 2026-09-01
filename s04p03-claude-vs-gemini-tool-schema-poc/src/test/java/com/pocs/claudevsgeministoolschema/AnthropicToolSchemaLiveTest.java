package com.pocs.claudevsgeministoolschema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class AnthropicToolSchemaLiveTest {

    @Autowired
    private ToolSchemaService toolSchemaService;

    @Test
    void forcedToolChoiceAlwaysCallsGetWeather() {
        List<AssistantMessage.ToolCall> toolCalls =
                toolSchemaService.anthropicForcedToolChoice("Tell me a fun fact about the ocean.");

        assertThat(toolCalls).isNotEmpty();
        assertThat(toolCalls.get(0).name()).isEqualTo("get_weather");
    }

    @Test
    void parallelToolCallCanIssueMultipleCallsInOneTurn() {
        List<AssistantMessage.ToolCall> toolCalls = toolSchemaService.anthropicParallelToolCall(
                "What's the weather like in Paris and in Tokyo right now?");

        assertThat(toolCalls).isNotEmpty();
        assertThat(toolCalls).allMatch(call -> call.name().equals("get_weather"));
    }
}
