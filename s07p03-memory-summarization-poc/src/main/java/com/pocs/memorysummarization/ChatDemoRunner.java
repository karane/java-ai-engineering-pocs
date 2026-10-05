package com.pocs.memorysummarization;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class ChatDemoRunner implements CommandLineRunner {

    private static final String SESSION_ID = "default-session";

    private final ChatClient chatClient;
    private final ChatClient rawChatClient;

    @Value("${app.provider}")
    private String activeProvider;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${spring.ai.google.genai.api-key:demo-placeholder-not-a-real-key}")
    private String geminiApiKey;

    public ChatDemoRunner(ChatClient rawChatClient) {
        this.rawChatClient = rawChatClient;
        // The memory stores the last 4 messages. Beyond that it summarizes.
        SummarizingChatMemory memory = new SummarizingChatMemory(rawChatClient, 4);

        this.chatClient = rawChatClient.mutate()
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
                .defaultAdvisors(a -> a.param(ChatMemory.CONVERSATION_ID, SESSION_ID))
                .build();
    }

    @Override
    public void run(String... args) {
        System.out.println("[s07p03] Active provider profile: " + activeProvider);

        boolean keyMissing = ("claude".equals(activeProvider) && anthropicApiKey.isBlank())
                || ("gemini".equals(activeProvider) && "demo-placeholder-not-a-real-key".equals(geminiApiKey));

        if (keyMissing) {
            System.out.println("[s07p03] API key for '" + activeProvider + "' not set — skipping demo call.");
            return;
        }

        System.out.println("Starting conversation (type 'exit' to quit):");
        Scanner scanner = new Scanner(System.in);
        
        // Simulating a conversation to trigger summarization
        String[] defaultPrompts = {
            "Hi, my name is Alice.",
            "I love programming in Java.",
            "I'm learning about Spring AI.",
            "Can you remind me what my name is and what I like?"
        };

        for (String prompt : defaultPrompts) {
            System.out.println("User: " + prompt);
            String response = chatClient.prompt().user(prompt).call().content();
            System.out.println("Assistant: " + response);
        }
    }
}
