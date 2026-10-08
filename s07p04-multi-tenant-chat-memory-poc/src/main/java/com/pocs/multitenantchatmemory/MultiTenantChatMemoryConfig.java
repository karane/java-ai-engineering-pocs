package com.pocs.multitenantchatmemory;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;

@Configuration
public class MultiTenantChatMemoryConfig {

    @Bean(name = "chatClientBuilder")
    @ConditionalOnProperty(prefix = "app", name = "provider", havingValue = "claude", matchIfMissing = true)
    public ChatClient.Builder claudeChatClientBuilder(AnthropicChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }

    @Bean(name = "chatClientBuilder")
    @ConditionalOnProperty(prefix = "app", name = "provider", havingValue = "gemini")
    public ChatClient.Builder geminiChatClientBuilder(GoogleGenAiChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }

    @Bean
    public ChatMemory tenantAChatMemory(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        // Tenant A: 10 minutes TTL
        // Note: For a real app, you would dynamically create or select these based on a TenantContext
        return new RedisChatMemory(redisTemplate, objectMapper, "tenant-a", Duration.ofMinutes(10));
    }

    @Bean
    public ChatMemory tenantBChatMemory(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        // Tenant B: 30 minutes TTL
        return new RedisChatMemory(redisTemplate, objectMapper, "tenant-b", Duration.ofMinutes(30));
    }

    @Bean
    public ChatClient tenantAChatClient(ChatClient.Builder chatClientBuilder, ChatMemory tenantAChatMemory) {
        return chatClientBuilder
            .clone()
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(tenantAChatMemory).build())
            .build();
    }

    @Bean
    public ChatClient tenantBChatClient(ChatClient.Builder chatClientBuilder, ChatMemory tenantBChatMemory) {
        return chatClientBuilder
            .clone()
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(tenantBChatMemory).build())
            .build();
    }
}
