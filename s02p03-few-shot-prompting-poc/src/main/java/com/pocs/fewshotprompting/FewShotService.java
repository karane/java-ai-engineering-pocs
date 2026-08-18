package com.pocs.fewshotprompting;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates two ways of building a few-shot classification prompt, and
 * compares the resulting classification quality (zero-shot vs. few-shot)
 * across the two providers wired in ChatClientConfig.
 *
 * The task: classify a short piece of customer-support text into exactly one
 * of "billing", "bug", "feature-request", or "praise".
 */
@Service
public class FewShotService {

    private static final String CATEGORIES = "billing, bug, feature-request, praise";

    private static final String LOOP_TEMPLATE = """
            Classify the customer-support message into exactly one category: {categories}.
            Respond with only the category name, nothing else.

            Examples:
            {examples}
            Message: {query}
            Category:""";

    private final ChatClient anthropicChatClient;
    private final ChatClient geminiChatClient;

    public FewShotService(@Qualifier("anthropicChatClient") ChatClient anthropicChatClient,
                           @Qualifier("geminiChatClient") ChatClient geminiChatClient) {
        this.anthropicChatClient = anthropicChatClient;
        this.geminiChatClient = geminiChatClient;
    }

    public Prompt buildFewShotMessagePrompt(String query, List<ClassificationExample> examples) {
        List<Message> messages = new ArrayList<>();
        for (ClassificationExample example : examples) {
            messages.add(new UserMessage("Classify: " + example.text()));
            messages.add(new AssistantMessage(example.label()));
        }
        messages.add(new UserMessage("Classify: " + query));
        return new Prompt(messages);
    }

    public String buildFewShotTemplatePrompt(String query, List<ClassificationExample> examples) {
        StringBuilder examplesBlock = new StringBuilder();
        for (ClassificationExample example : examples) {
            examplesBlock.append("Message: ").append(example.text()).append('\n');
            examplesBlock.append("Category: ").append(example.label()).append("\n\n");
        }

        PromptTemplate promptTemplate = PromptTemplate.builder().template(LOOP_TEMPLATE).build();
        return promptTemplate.render(Map.of(
                "categories", CATEGORIES,
                "examples", examplesBlock.toString(),
                "query", query));
    }

    private String zeroShotPrompt(String query) {
        return "Classify the customer-support message into exactly one category: " + CATEGORIES
                + ". Respond with only the category name, nothing else.\n\nMessage: " + query + "\nCategory:";
    }

    public String classifyZeroShotAnthropic(String query) {
        return anthropicChatClient.prompt().user(zeroShotPrompt(query)).call().content();
    }

    public String classifyZeroShotGemini(String query) {
        return geminiChatClient.prompt().user(zeroShotPrompt(query)).call().content();
    }

    public String classifyFewShotAnthropic(String query, List<ClassificationExample> examples) {
        return anthropicChatClient.prompt(buildFewShotMessagePrompt(query, examples)).call().content();
    }

    public String classifyFewShotGemini(String query, List<ClassificationExample> examples) {
        return geminiChatClient.prompt(buildFewShotMessagePrompt(query, examples)).call().content();
    }

    public ClassificationComparison compare(String query, List<ClassificationExample> examples) {
        return new ClassificationComparison(
                query,
                classifyZeroShotAnthropic(query),
                classifyFewShotAnthropic(query, examples),
                classifyZeroShotGemini(query),
                classifyFewShotGemini(query, examples));
    }

    public record ClassificationComparison(String query,
                                            String anthropicZeroShot,
                                            String anthropicFewShot,
                                            String geminiZeroShot,
                                            String geminiFewShot) {
    }
}
