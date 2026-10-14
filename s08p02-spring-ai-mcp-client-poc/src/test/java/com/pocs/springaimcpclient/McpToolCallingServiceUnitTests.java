package com.pocs.springaimcpclient;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class McpToolCallingServiceUnitTests {

    @Test
    void discoveredToolNamesReflectsWhatTheProviderReturns() {
        ToolCallback celsiusTool = mock(ToolCallback.class);
        ToolDefinition celsiusDefinition = mock(ToolDefinition.class);
        when(celsiusDefinition.name()).thenReturn("celsiusToFahrenheit");
        when(celsiusTool.getToolDefinition()).thenReturn(celsiusDefinition);

        ToolCallback kmTool = mock(ToolCallback.class);
        ToolDefinition kmDefinition = mock(ToolDefinition.class);
        when(kmDefinition.name()).thenReturn("kilometersToMiles");
        when(kmTool.getToolDefinition()).thenReturn(kmDefinition);

        ToolCallbackProvider provider = () -> new ToolCallback[] {celsiusTool, kmTool};
        ChatClient chatClient = mock(ChatClient.class);

        McpToolCallingService service = new McpToolCallingService(chatClient, provider);

        assertThat(service.discoveredToolNames())
                .containsExactlyInAnyOrder("celsiusToFahrenheit", "kilometersToMiles");
    }

    @Test
    void noOpProviderExposesNoTools() {
        ToolCallbackProvider provider = () -> new ToolCallback[0];
        ChatClient chatClient = mock(ChatClient.class);

        McpToolCallingService service = new McpToolCallingService(chatClient, provider);

        assertThat(service.discoveredToolNames()).isEmpty();
    }
}
