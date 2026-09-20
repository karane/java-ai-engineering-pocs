package com.pocs.contextualcompression;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

/** A fully deterministic stand-in for a real cross-encoder-style scoring call: it parses the {@code QUERY:}/{@code PASSAGE:} prompt built by {@link RerankingService}, counts how many significant words the two share, and returns that count (capped at 10) as the "relevance score" text response - exactly the single-integer format the real prompt asks for. */
class DeterministicScoringChatModel implements ChatModel {

    private static final Pattern QUERY_PASSAGE = Pattern.compile(
            "QUERY:\\s*(?<query>.*?)\\s*PASSAGE:\\s*(?<passage>.*)", Pattern.DOTALL);
    private static final Pattern WORD = Pattern.compile("[a-zA-Z]{4,}");

    @Override
    public ChatResponse call(Prompt prompt) {
        String promptText = prompt.getInstructions().stream()
                .map(Message::getText)
                .collect(Collectors.joining("\n"));

        int score = scoreFor(promptText);
        Generation generation = new Generation(new AssistantMessage(String.valueOf(score)));
        return new ChatResponse(List.of(generation));
    }

    private int scoreFor(String promptText) {
        Matcher matcher = QUERY_PASSAGE.matcher(promptText);
        if (!matcher.find()) {
            return 0;
        }
        Set<String> queryWords = wordsOf(matcher.group("query"));
        Set<String> passageWords = wordsOf(matcher.group("passage"));
        long shared = queryWords.stream().filter(passageWords::contains).count();
        return (int) Math.min(10, shared * 3);
    }

    private Set<String> wordsOf(String text) {
        Set<String> words = new java.util.HashSet<>();
        Matcher matcher = WORD.matcher(text.toLowerCase());
        while (matcher.find()) {
            words.add(matcher.group());
        }
        return words;
    }
}
