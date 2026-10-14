package com.pocs.springaimcpclient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

/** Calls Claude with tools discovered from the s08p01 MCP server over a stdio subprocess. */
@Service
public class McpToolCallingService {

    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpToolCallbackProvider;

    public McpToolCallingService(ChatClient anthropicChatClient, ToolCallbackProvider mcpToolCallbackProvider) {
        this.chatClient = anthropicChatClient;
        this.mcpToolCallbackProvider = mcpToolCallbackProvider;
    }

    public String ask(String question) {
        ToolCallback[] mcpTools = mcpToolCallbackProvider.getToolCallbacks();
        return chatClient.prompt()
                .user(question)
                .toolCallbacks(mcpTools)
                .call()
                .content();
    }

    public String[] discoveredToolNames() {
        ToolCallback[] mcpTools = mcpToolCallbackProvider.getToolCallbacks();
        String[] names = new String[mcpTools.length];
        for (int i = 0; i < mcpTools.length; i++) {
            names[i] = mcpTools[i].getToolDefinition().name();
        }
        return names;
    }
}
