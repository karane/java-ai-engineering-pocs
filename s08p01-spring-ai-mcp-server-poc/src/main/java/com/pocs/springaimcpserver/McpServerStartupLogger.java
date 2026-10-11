package com.pocs.springaimcpserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class McpServerStartupLogger implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(McpServerStartupLogger.class);

    private final ToolCallbackProvider toolCallbackProvider;

    public McpServerStartupLogger(ToolCallbackProvider toolCallbackProvider) {
        this.toolCallbackProvider = toolCallbackProvider;
    }

    @Override
    public void run(String... args) {
        ToolCallback[] callbacks = toolCallbackProvider.getToolCallbacks();
        String names = Arrays.stream(callbacks)
                .map(callback -> callback.getToolDefinition().name())
                .collect(Collectors.joining(", "));
        logger.warn("[s08p01] MCP server up - {} tool(s) registered as SyncToolSpecifications: {}",
                callbacks.length, names);
    }
}
