package com.pocs.claudetoolusejson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StructuredOutputService {

    private final ChatClient anthropicChatClient;
    private final ChatClient geminiChatClient;
    private final InvoiceExtractionTool invoiceExtractionTool;
    private final BeanOutputConverter<Invoice> invoiceConverter = new BeanOutputConverter<>(Invoice.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StructuredOutputService(ChatClient anthropicChatClient, ChatClient geminiChatClient,
            InvoiceExtractionTool invoiceExtractionTool) {
        this.anthropicChatClient = anthropicChatClient;
        this.geminiChatClient = geminiChatClient;
        this.invoiceExtractionTool = invoiceExtractionTool;
    }

    public Invoice anthropicForcedToolUse(String invoiceDescription) {
        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .toolNames("record_invoice")
                .toolChoice(new AnthropicApi.ToolChoiceTool("record_invoice"))
                .internalToolExecutionEnabled(false)
                .build();

        var chatResponse = anthropicChatClient.prompt()
                .user(invoiceDescription)
                .tools(invoiceExtractionTool)
                .options(options)
                .call()
                .chatResponse();

        List<AssistantMessage.ToolCall> toolCalls = chatResponse.getResults().stream()
                .flatMap(generation -> generation.getOutput().getToolCalls().stream())
                .toList();
        if (toolCalls.isEmpty()) {
            throw new IllegalStateException("Claude did not call record_invoice despite the forced toolChoice");
        }
        return parseInvoiceArguments(toolCalls.get(0).arguments());
    }

    private Invoice parseInvoiceArguments(String argumentsJson) {
        try {
            return objectMapper.readValue(argumentsJson, Invoice.class);
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse record_invoice tool call arguments: " + argumentsJson, e);
        }
    }

    public Invoice geminiNativeJsonMode(String invoiceDescription) {
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .responseMimeType("application/json")
                .responseSchema(invoiceConverter.getJsonSchema())
                .build();

        return geminiChatClient.prompt()
                .user(invoiceDescription)
                .options(options)
                .call()
                .entity(Invoice.class);
    }

    public Invoice promptEngineeredJson(ChatClient chatClient, String invoiceDescription) {
        String prompt = invoiceDescription + System.lineSeparator() + System.lineSeparator()
                + invoiceConverter.getFormat();

        String raw = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return invoiceConverter.convert(raw);
    }

    public ChatClient anthropicChatClient() {
        return anthropicChatClient;
    }

    public ChatClient geminiChatClient() {
        return geminiChatClient;
    }
}
