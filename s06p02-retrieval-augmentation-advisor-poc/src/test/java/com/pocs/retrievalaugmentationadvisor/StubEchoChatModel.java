package com.pocs.retrievalaugmentationadvisor;

import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

/** A deterministic, zero-network {@link ChatModel} stub, matching {@code s06p03}'s {@code StubEchoChatModel}: it never calls any LLM, it just echoes back the full text of every message the {@link Prompt} carries. */
class StubEchoChatModel implements ChatModel {

    @Override
    public ChatResponse call(Prompt prompt) {
        StringBuilder echoed = new StringBuilder();
        for (Message message : prompt.getInstructions()) {
            echoed.append(message.getText()).append('\n');
        }
        AssistantMessage assistantMessage = new AssistantMessage(echoed.toString());
        Generation generation = new Generation(assistantMessage, ChatGenerationMetadata.NULL);
        return new ChatResponse(List.of(generation));
    }
}
