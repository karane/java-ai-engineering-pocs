package com.pocs.toolcontext;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ToolContextService {

    private final ChatClient chatClient;
    private final AnthropicChatModel chatModel;
    private final ToolCallingManager toolCallingManager;
    private final AccountLookupTool accountLookupTool;

    public ToolContextService(ChatClient chatClient, AnthropicChatModel chatModel,
            ToolCallingManager toolCallingManager, AccountLookupTool accountLookupTool) {
        this.chatClient = chatClient;
        this.chatModel = chatModel;
        this.toolCallingManager = toolCallingManager;
        this.accountLookupTool = accountLookupTool;
    }

    public String automaticToolLoop(String userId, String question) {
        return chatClient.prompt()
                .user(question)
                .tools(accountLookupTool)
                .toolContext(Map.of("userId", userId))
                .call()
                .content();
    }

    public String manualToolLoop(String userId, String question) {
        ToolCallback[] toolCallbacks = ToolCallbacks.from(accountLookupTool);

        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .toolCallbacks(toolCallbacks)
                .internalToolExecutionEnabled(false)
                .toolContext(Map.of("userId", userId))
                .build();

        Prompt prompt = new Prompt(new UserMessage(question), options);
        ChatResponse response = chatModel.call(prompt);

        if (!response.hasToolCalls()) {
            return response.getResult().getOutput().getText();
        }

        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, response);

        List<Message> conversationHistory = toolExecutionResult.conversationHistory();
        Prompt followUpPrompt = new Prompt(conversationHistory, options);
        ChatResponse followUpResponse = chatModel.call(followUpPrompt);

        return followUpResponse.getResult().getOutput().getText();
    }
}
