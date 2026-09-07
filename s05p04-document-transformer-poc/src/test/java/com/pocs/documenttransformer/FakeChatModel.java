package com.pocs.documenttransformer;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.AssistantMessage;

class FakeChatModel implements ChatModel {

    private final String response;

    FakeChatModel(String response) {
        this.response = response;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        Generation generation = new Generation(new AssistantMessage(response));
        return new ChatResponse(java.util.List.of(generation));
    }
}
