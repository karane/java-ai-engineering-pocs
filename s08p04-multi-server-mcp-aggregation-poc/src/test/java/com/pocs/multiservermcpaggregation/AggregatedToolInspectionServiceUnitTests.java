package com.pocs.multiservermcpaggregation;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AggregatedToolInspectionServiceUnitTests {

    @Test
    void detectsTheDeliberatePingNameCollisionAcrossBothServers() {
        ToolCallback serverAPing = toolCallbackNamed("ping");
        ToolCallback serverBPing = toolCallbackNamed("ping");
        ToolCallback celsiusTool = toolCallbackNamed("celsiusToFahrenheit");
        ToolCallback shoutTool = toolCallbackNamed("shout");

        ToolCallbackProvider provider = () -> new ToolCallback[] {serverAPing, serverBPing, celsiusTool, shoutTool};
        ChatClient chatClient = mock(ChatClient.class);

        AggregatedToolInspectionService service = new AggregatedToolInspectionService(chatClient, provider);

        assertThat(service.toolsGroupedByName()).hasSize(3);
        assertThat(service.toolsGroupedByName().get("ping")).hasSize(2);
        assertThat(service.duplicateToolNames()).containsExactly("ping");
    }

    @Test
    void noDuplicatesWhenEveryToolNameIsUnique() {
        ToolCallbackProvider provider = () -> new ToolCallback[] {
                toolCallbackNamed("celsiusToFahrenheit"),
                toolCallbackNamed("shout"),
                toolCallbackNamed("wordCount")
        };
        ChatClient chatClient = mock(ChatClient.class);

        AggregatedToolInspectionService service = new AggregatedToolInspectionService(chatClient, provider);

        assertThat(service.duplicateToolNames()).isEmpty();
    }

    private ToolCallback toolCallbackNamed(String name) {
        ToolCallback callback = mock(ToolCallback.class);
        ToolDefinition definition = mock(ToolDefinition.class);
        when(definition.name()).thenReturn(name);
        when(callback.getToolDefinition()).thenReturn(definition);
        return callback;
    }
}
