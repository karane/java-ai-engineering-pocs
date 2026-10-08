package com.pocs.multitenantchatmemory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import org.springframework.ai.chat.memory.ChatMemory;

@Component
public class DemoRunner implements CommandLineRunner {

    private final ChatClient tenantAChatClient;
    private final ChatClient tenantBChatClient;
    private final boolean hasAnthropicKey;
    private final boolean hasGeminiKey;

    public DemoRunner(
            @Qualifier("tenantAChatClient") ChatClient tenantAChatClient,
            @Qualifier("tenantBChatClient") ChatClient tenantBChatClient,
            @Value("${anthropic.api-key:}") String anthropicKey,
            @Value("${gemini.api-key:demo-placeholder-not-a-real-key}") String geminiKey) {
        this.tenantAChatClient = tenantAChatClient;
        this.tenantBChatClient = tenantBChatClient;
        this.hasAnthropicKey = anthropicKey != null && !anthropicKey.isBlank();
        this.hasGeminiKey = geminiKey != null && !geminiKey.equals("demo-placeholder-not-a-real-key") && !geminiKey.isBlank();
    }

    @Override
    public void run(String... args) throws Exception {
        if (!hasAnthropicKey && !hasGeminiKey) {
            System.out.println("[s07p04] No API keys found. Skipping live demo.");
            return;
        }

        System.out.println("\n[s07p04] Starting Multi-Tenant Chat Memory Demo...\n");

        String conversationId = "user-123";

        System.out.println("[s07p04] --- Tenant A (10m TTL) ---");
        String responseA1 = tenantAChatClient.prompt()
                .user("Hi, I am Alice and my favorite color is blue.")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
        System.out.println("[s07p04] Tenant A Turn 1: " + responseA1);

        String responseA2 = tenantAChatClient.prompt()
                .user("What is my name and favorite color?")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
        System.out.println("[s07p04] Tenant A Turn 2: " + responseA2);


        System.out.println("\n[s07p04] --- Tenant B (30m TTL) ---");
        // Use the exact same conversation ID for Tenant B, but memory should be isolated
        String responseB1 = tenantBChatClient.prompt()
                .user("Hi, I am Bob and my favorite color is red.")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
        System.out.println("[s07p04] Tenant B Turn 1: " + responseB1);

        String responseB2 = tenantBChatClient.prompt()
                .user("What is my name and favorite color?")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
        System.out.println("[s07p04] Tenant B Turn 2: " + responseB2);

        System.out.println("\n[s07p04] --- Verifying Isolation for Tenant A ---");
        String responseA3 = tenantAChatClient.prompt()
                .user("Just to double check, what is my name?")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
        System.out.println("[s07p04] Tenant A Turn 3 (Should still be Alice/blue): " + responseA3);

        System.out.println("\n[s07p04] Demo complete.");
    }
}
