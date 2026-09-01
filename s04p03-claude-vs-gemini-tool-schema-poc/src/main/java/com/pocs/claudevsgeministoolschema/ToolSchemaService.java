package com.pocs.claudevsgeministoolschema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.types.FunctionDeclaration;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ToolSchemaService {

    private final ChatClient anthropicChatClient;
    private final ChatClient geminiChatClient;
    private final WeatherLookupTool weatherLookupTool;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ToolSchemaService(ChatClient anthropicChatClient, ChatClient geminiChatClient,
            WeatherLookupTool weatherLookupTool) {
        this.anthropicChatClient = anthropicChatClient;
        this.geminiChatClient = geminiChatClient;
        this.weatherLookupTool = weatherLookupTool;
    }

    public ToolDefinition sharedToolDefinition() {
        ToolCallback[] callbacks = MethodToolCallbackProvider.builder()
                .toolObjects(weatherLookupTool)
                .build()
                .getToolCallbacks();
        return callbacks[0].getToolDefinition();
    }

    public String anthropicToolSchemaJson() throws Exception {
        ToolDefinition definition = sharedToolDefinition();
        Map<String, Object> inputSchema = objectMapper.readValue(definition.inputSchema(), Map.class);
        AnthropicApi.Tool tool = new AnthropicApi.Tool(definition.name(), definition.description(), inputSchema);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tool);
    }

    public String geminiFunctionDeclarationJson() throws Exception {
        ToolDefinition definition = sharedToolDefinition();
        Map<String, Object> inputSchema = objectMapper.readValue(definition.inputSchema(), Map.class);
        FunctionDeclaration declaration = FunctionDeclaration.builder()
                .name(definition.name())
                .description(definition.description())
                .parametersJsonSchema(inputSchema)
                .build();
        return declaration.toJson();
    }

    public List<AssistantMessage.ToolCall> anthropicParallelToolCall(String multiPartQuestion) {
        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();
        var response = anthropicChatClient.prompt()
                .user(multiPartQuestion)
                .tools(weatherLookupTool)
                .options(options)
                .call()
                .chatResponse();
        return response.getResults().stream()
                .flatMap(generation -> generation.getOutput().getToolCalls().stream())
                .toList();
    }

    public List<AssistantMessage.ToolCall> geminiParallelToolCall(String multiPartQuestion) {
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();
        var response = geminiChatClient.prompt()
                .user(multiPartQuestion)
                .tools(weatherLookupTool)
                .options(options)
                .call()
                .chatResponse();
        return response.getResults().stream()
                .flatMap(generation -> generation.getOutput().getToolCalls().stream())
                .toList();
    }

    public List<AssistantMessage.ToolCall> anthropicForcedToolChoice(String question) {
        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .toolNames("get_weather")
                .toolChoice(new AnthropicApi.ToolChoiceTool("get_weather"))
                .internalToolExecutionEnabled(false)
                .build();
        var response = anthropicChatClient.prompt()
                .user(question)
                .tools(weatherLookupTool)
                .options(options)
                .call()
                .chatResponse();
        return response.getResults().stream()
                .flatMap(generation -> generation.getOutput().getToolCalls().stream())
                .toList();
    }

    public List<AssistantMessage.ToolCall> geminiRestrictedToolNames(String question) {
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .toolName("get_weather")
                .internalToolExecutionEnabled(false)
                .build();
        var response = geminiChatClient.prompt()
                .user(question)
                .tools(weatherLookupTool)
                .options(options)
                .call()
                .chatResponse();
        return response.getResults().stream()
                .flatMap(generation -> generation.getOutput().getToolCalls().stream())
                .toList();
    }

    public ChatClient anthropicChatClient() {
        return anthropicChatClient;
    }

    public ChatClient geminiChatClient() {
        return geminiChatClient;
    }
}
