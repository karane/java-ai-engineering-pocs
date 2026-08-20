package com.pocs.resourcepromptcatalog;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Thin service that renders a catalog template by key and sends it through the
 * (single, Claude-backed) ChatClient. Kept separate from PromptTemplateRegistry
 * so the registry itself stays provider-agnostic and independently testable
 * without a live API call.
 */
@Service
public class CatalogChatService {

    private final ChatClient chatClient;
    private final PromptTemplateRegistry registry;

    public CatalogChatService(ChatClient chatClient, PromptTemplateRegistry registry) {
        this.chatClient = chatClient;
        this.registry = registry;
    }

    /** Renders the template at {@code key} and calls the model with it. */
    public String ask(String key, Map<String, Object> variables) {
        String prompt = registry.render(key, variables);
        return chatClient.prompt().user(prompt).call().content();
    }

    /**
     * Runs both versions of a prompt (e.g. "summarize:v1" vs "summarize:v2")
     * against the same input and returns each version's response, keyed the
     * same way as the rendered templates - the A/B comparison the catalog
     * exists to support.
     */
    public Map<String, String> compareVersions(String baseKey, Map<String, Object> variables) {
        Map<String, String> renderedPrompts = registry.renderVersionsForComparison(baseKey, variables);
        Map<String, String> responses = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : renderedPrompts.entrySet()) {
            String response = chatClient.prompt().user(entry.getValue()).call().content();
            responses.put(entry.getKey(), response);
        }
        return responses;
    }
}
