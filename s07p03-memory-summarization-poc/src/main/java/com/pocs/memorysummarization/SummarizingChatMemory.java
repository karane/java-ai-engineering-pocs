package com.pocs.memorysummarization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SummarizingChatMemory implements ChatMemory {

    private static final Logger log = LoggerFactory.getLogger(SummarizingChatMemory.class);

    private final ChatClient chatClient;
    private final int maxMessages;
    private final Map<String, List<Message>> memory = new ConcurrentHashMap<>();

    public SummarizingChatMemory(ChatClient chatClient, int maxMessages) {
        this.chatClient = chatClient;
        this.maxMessages = maxMessages;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        memory.putIfAbsent(conversationId, new ArrayList<>());
        List<Message> currentMessages = memory.get(conversationId);
        currentMessages.addAll(messages);

        if (currentMessages.size() > maxMessages) {
            summarize(conversationId, currentMessages);
        }
    }

    private void summarize(String conversationId, List<Message> currentMessages) {
        // Extract the messages to summarize (e.g. all but the last 2)
        int keep = 2;
        List<Message> toSummarize = currentMessages.subList(0, currentMessages.size() - keep);
        List<Message> toKeep = new ArrayList<>(currentMessages.subList(currentMessages.size() - keep, currentMessages.size()));

        StringBuilder conversationText = new StringBuilder();
        for (Message m : toSummarize) {
            conversationText.append(m.getMessageType().getValue()).append(": ").append(m.getText()).append("\n");
        }

        String summaryPrompt = "Please summarize the following conversation concisely:\n" + conversationText;

        log.info("[s07p03] Summarizing {} message(s) for conversation '{}':\n----- INPUT TO SUMMARIZER -----\n{}--------------------------------",
                toSummarize.size(), conversationId, conversationText);

        String summary = chatClient.prompt()
                .user(summaryPrompt)
                .call()
                .content();

        log.info("[s07p03] Summary produced for conversation '{}':\n----- SUMMARIZER OUTPUT -----\n{}\n------------------------------",
                conversationId, summary);

        List<Message> newMessages = new ArrayList<>();
        newMessages.add(new SystemMessage("Previous conversation summary: " + summary));
        newMessages.addAll(toKeep);

        memory.put(conversationId, newMessages);
    }

    @Override
    public List<Message> get(String conversationId) {
        return new ArrayList<>(memory.getOrDefault(conversationId, new ArrayList<>()));
    }

    @Override
    public void clear(String conversationId) {
        memory.remove(conversationId);
    }
}
