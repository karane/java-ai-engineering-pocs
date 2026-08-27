package com.pocs.springaitoolmethod;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ToolCallingService {

    private final ChatClient anthropicChatClient;
    private final ChatClient geminiChatClient;
    private final CalculatorTools calculatorTools;
    private final TimeTools timeTools;

    public ToolCallingService(ChatClient anthropicChatClient, ChatClient geminiChatClient,
            CalculatorTools calculatorTools, TimeTools timeTools) {
        this.anthropicChatClient = anthropicChatClient;
        this.geminiChatClient = geminiChatClient;
        this.calculatorTools = calculatorTools;
        this.timeTools = timeTools;
    }

    public String askAnthropic(String question) {
        return anthropicChatClient.prompt()
                .user(question)
                .tools(calculatorTools)
                .toolCallbacks(timeTools.currentDateTimeToolCallback())
                .call()
                .content();
    }

    public String askGemini(String question) {
        return geminiChatClient.prompt()
                .user(question)
                .tools(calculatorTools)
                .toolCallbacks(timeTools.currentDateTimeToolCallback())
                .call()
                .content();
    }
}
