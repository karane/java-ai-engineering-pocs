package com.pocs.jdbcchatmemoryrepository;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JpaChatMemory implements ChatMemory {

    private final ChatMessageRepository repository;

    public JpaChatMemory(ChatMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        messages.forEach(msg -> add(conversationId, msg));
    }

    @Override
    public void add(String conversationId, Message message) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setConversationId(conversationId);
        entity.setMessageType(message.getMessageType().getValue());
        entity.setContent(message.getText());
        entity.setCreatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    @Override
    public List<Message> get(String conversationId) {
        return repository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::toMessage)
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        repository.deleteByConversationId(conversationId);
    }

    private Message toMessage(ChatMessageEntity entity) {
        String type = entity.getMessageType();
        if ("USER".equalsIgnoreCase(type)) {
            return new UserMessage(entity.getContent());
        } else if ("ASSISTANT".equalsIgnoreCase(type)) {
            return new AssistantMessage(entity.getContent());
        } else if ("SYSTEM".equalsIgnoreCase(type)) {
            return new SystemMessage(entity.getContent());
        } else {
            // fallback
            return new UserMessage(entity.getContent());
        }
    }
}
