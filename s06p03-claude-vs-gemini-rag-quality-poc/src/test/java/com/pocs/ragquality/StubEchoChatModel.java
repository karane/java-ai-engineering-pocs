package com.pocs.ragquality;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

/** A deterministic, zero-network {@link ChatModel} stub used only by {@link GoldenDatasetHarnessTest}. */
class StubEchoChatModel implements ChatModel {

    @Override
    public ChatResponse call(Prompt prompt) {
        StringBuilder echoed = new StringBuilder();
        for (Message message : prompt.getInstructions()) {
            echoed.append(message.getText()).append('\n');
        }
        AssistantMessage assistantMessage = new AssistantMessage(echoed.toString());
        Generation generation = new Generation(assistantMessage, ChatGenerationMetadata.NULL);
        return new ChatResponse(java.util.List.of(generation));
    }
}
