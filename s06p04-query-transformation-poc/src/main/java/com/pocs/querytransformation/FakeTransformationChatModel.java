package com.pocs.querytransformation;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

/** A deterministic, offline test double for {@link ChatModel}. */
public class FakeTransformationChatModel implements ChatModel {

    private static final Pattern NUMBER_OF_VARIANTS = Pattern.compile("generate (\\d+) different versions");

    private static final List<String> FILLER_PHRASES = List.of(
            "um, ", "so, ", "basically, ", "can you please tell me ", "i would like to know ",
            "please tell me about ", "could you explain ");

    @Override
    public ChatResponse call(Prompt prompt) {
        String userText = lastUserMessageText(prompt);
        String reply = respondTo(userText);
        return new ChatResponse(List.of(new Generation(new AssistantMessage(reply))));
    }

    private String lastUserMessageText(Prompt prompt) {
        List<Message> messages = prompt.getInstructions();
        return messages.get(messages.size() - 1).getText();
    }

    private String respondTo(String renderedPrompt) {
        if (renderedPrompt.contains("standalone query") && renderedPrompt.contains("Conversation history:")) {
            return compress(renderedPrompt);
        }
        if (renderedPrompt.contains("Rewritten query:")) {
            return rewrite(renderedPrompt);
        }
        if (renderedPrompt.contains("Query variants:")) {
            return expand(renderedPrompt);
        }
        return renderedPrompt;
    }

    /** CompressionQueryTransformer: fold conversation history + follow-up into one standalone query. */
    private String compress(String renderedPrompt) {
        String history = between(renderedPrompt, "Conversation history:", "Follow-up query:").trim();
        // The template's own rendered text ends with "{query}\nStandalone query:" - that trailing
        // label is part of the prompt, not the follow-up query itself, so it's stripped here.
        String followUp = between(renderedPrompt, "Follow-up query:", "Standalone query:").trim();
        String historySummary = history.replaceAll("\\s+", " ").trim();
        return "Standalone query - context: [" + historySummary + "] question: " + followUp;
    }

    /** RewriteQueryTransformer: strip conversational filler, keeping the query concise and specific. */
    private String rewrite(String renderedPrompt) {
        String original = between(renderedPrompt, "Original query:", "Rewritten query:").trim();
        String rewritten = original;
        for (String filler : FILLER_PHRASES) {
            rewritten = rewritten.replaceAll("(?i)" + Pattern.quote(filler), "");
        }
        rewritten = rewritten.replaceAll("\\s+", " ").trim();
        if (rewritten.endsWith("?")) {
            rewritten = rewritten.substring(0, rewritten.length() - 1);
        }
        return rewritten;
    }

    /** MultiQueryExpander: fan a single query out into N deterministic variant phrasings. */
    private String expand(String renderedPrompt) {
        Matcher matcher = NUMBER_OF_VARIANTS.matcher(renderedPrompt);
        int numberOfQueries = matcher.find() ? Integer.parseInt(matcher.group(1)) : 3;
        String original = after(renderedPrompt, "Original query:").trim();
        original = original.substring(0, original.indexOf("Query variants:")).trim();

        StringBuilder variants = new StringBuilder();
        String[] angles = {"an overview of", "practical examples of", "the technical details of",
                "common pitfalls with", "alternatives to"};
        for (int i = 0; i < numberOfQueries; i++) {
            String angle = angles[i % angles.length];
            if (i > 0) {
                variants.append("\n");
            }
            variants.append(angle).append(" ").append(original);
        }
        return variants.toString();
    }

    private String between(String text, String startMarker, String endMarker) {
        int start = text.indexOf(startMarker) + startMarker.length();
        int end = text.indexOf(endMarker, start);
        return text.substring(start, end);
    }

    private String after(String text, String marker) {
        return text.substring(text.indexOf(marker) + marker.length());
    }
}
