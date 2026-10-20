package com.pocs.multiservermcpaggregation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Aggregates tools from both MCP servers and inspects the combined list for name collisions. */
@Service
@Profile("!server-a & !server-b")
public class AggregatedToolInspectionService {

    private final ChatClient chatClient;
    private final ToolCallbackProvider aggregatedToolCallbackProvider;

    public AggregatedToolInspectionService(ChatClient anthropicChatClient, ToolCallbackProvider aggregatedToolCallbackProvider) {
        this.chatClient = anthropicChatClient;
        this.aggregatedToolCallbackProvider = aggregatedToolCallbackProvider;
    }

    /** Groups the aggregated tool list by name, so callers can see which names collided. */
    public Map<String, List<ToolCallback>> toolsGroupedByName() {
        Map<String, List<ToolCallback>> grouped = new HashMap<>();
        for (ToolCallback callback : aggregatedToolCallbackProvider.getToolCallbacks()) {
            grouped.computeIfAbsent(callback.getToolDefinition().name(), k -> new ArrayList<>()).add(callback);
        }
        return grouped;
    }

    public List<String> duplicateToolNames() {
        List<String> duplicates = new ArrayList<>();
        toolsGroupedByName().forEach((name, callbacks) -> {
            if (callbacks.size() > 1) {
                duplicates.add(name);
            }
        });
        return duplicates;
    }

    public String ask(String question) {
        ToolCallback[] allTools = aggregatedToolCallbackProvider.getToolCallbacks();
        return chatClient.prompt()
                .user(question)
                .toolCallbacks(allTools)
                .call()
                .content();
    }
}
