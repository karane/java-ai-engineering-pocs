package com.pocs.multitenantchatmemory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RedisChatMemory implements ChatMemory {

    private record StoredMessage(String type, String text) {
    }

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;
    private final String tenantId;

    public RedisChatMemory(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, String tenantId, Duration ttl) {
        Assert.notNull(redisTemplate, "RedisTemplate must not be null");
        Assert.notNull(objectMapper, "ObjectMapper must not be null");
        Assert.hasText(tenantId, "TenantId must not be empty");
        Assert.notNull(ttl, "TTL must not be null");
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.tenantId = tenantId;
        this.ttl = ttl;
    }

    private String getKey(String conversationId) {
        return "chat_memory:" + tenantId + ":" + conversationId;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        String key = getKey(conversationId);
        List<String> jsonMessages = new ArrayList<>();
        for (Message msg : messages) {
            try {
                StoredMessage stored = new StoredMessage(msg.getMessageType().getValue(), msg.getText());
                jsonMessages.add(objectMapper.writeValueAsString(stored));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize message", e);
            }
        }
        redisTemplate.opsForList().rightPushAll(key, jsonMessages);
        redisTemplate.expire(key, ttl);
    }

    @Override
    public List<Message> get(String conversationId) {
        String key = getKey(conversationId);
        List<String> jsonMessages = redisTemplate.opsForList().range(key, 0, -1);
        if (jsonMessages == null || jsonMessages.isEmpty()) {
            return Collections.emptyList();
        }

        List<Message> messages = new ArrayList<>();
        for (String json : jsonMessages) {
            try {
                StoredMessage stored = objectMapper.readValue(json, StoredMessage.class);
                messages.add(toMessage(stored));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize message", e);
            }
        }
        return messages;
    }

    private Message toMessage(StoredMessage stored) {
        MessageType type = MessageType.fromValue(stored.type());
        return switch (type) {
            case USER -> new UserMessage(stored.text());
            case ASSISTANT -> new AssistantMessage(stored.text());
            case SYSTEM -> new SystemMessage(stored.text());
            default -> new UserMessage(stored.text());
        };
    }

    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(getKey(conversationId));
    }
}
