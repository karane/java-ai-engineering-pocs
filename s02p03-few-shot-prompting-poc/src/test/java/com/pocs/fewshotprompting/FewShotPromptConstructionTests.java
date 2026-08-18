package com.pocs.fewshotprompting;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ungated unit tests: verify the few-shot Prompt/message list and the
 * PromptTemplate loop-based rendering are constructed correctly, with no
 * live API call involved.
 */
class FewShotPromptConstructionTests {

    private final FewShotService fewShotService = new FewShotService(
            org.springframework.ai.chat.client.ChatClient.create(new NoopChatModel()),
            org.springframework.ai.chat.client.ChatClient.create(new NoopChatModel()));

    private final List<ClassificationExample> examples = ClassificationExample.catalog();

    @Test
    void fewShotMessagePromptAlternatesUserAndAssistantMessagesInOrder() {
        Prompt prompt = fewShotService.buildFewShotMessagePrompt("A test query", examples);
        List<Message> messages = prompt.getInstructions();

        // 4 examples * 2 messages each + 1 final query message
        assertThat(messages).hasSize(examples.size() * 2 + 1);

        for (int i = 0; i < examples.size(); i++) {
            Message userMessage = messages.get(i * 2);
            Message assistantMessage = messages.get(i * 2 + 1);

            assertThat(userMessage.getMessageType()).isEqualTo(MessageType.USER);
            assertThat(userMessage.getText()).contains(examples.get(i).text());

            assertThat(assistantMessage.getMessageType()).isEqualTo(MessageType.ASSISTANT);
            assertThat(assistantMessage.getText()).isEqualTo(examples.get(i).label());
        }
    }

    @Test
    void fewShotMessagePromptEndsWithTheFinalUnansweredUserQuery() {
        Prompt prompt = fewShotService.buildFewShotMessagePrompt("Please classify this", examples);
        List<Message> messages = prompt.getInstructions();

        Message last = messages.get(messages.size() - 1);
        assertThat(last.getMessageType()).isEqualTo(MessageType.USER);
        assertThat(last).isInstanceOf(UserMessage.class);
        assertThat(last.getText()).contains("Please classify this");
    }

    @Test
    void fewShotMessagePromptWithNoExamplesIsJustTheQuery() {
        Prompt prompt = fewShotService.buildFewShotMessagePrompt("solo query", List.of());
        List<Message> messages = prompt.getInstructions();

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getMessageType()).isEqualTo(MessageType.USER);
        assertThat(messages.get(0).getText()).contains("solo query");
    }

    @Test
    void fewShotTemplatePromptInjectsEveryExampleAndTheQueryViaLoop() {
        String rendered = fewShotService.buildFewShotTemplatePrompt("Refund my order please", examples);

        assertThat(rendered).contains("billing, bug, feature-request, praise");
        assertThat(rendered).contains("Refund my order please");

        for (ClassificationExample example : examples) {
            assertThat(rendered).contains("Message: " + example.text());
            assertThat(rendered).contains("Category: " + example.label());
        }
    }

    @Test
    void fewShotTemplatePromptRendersExamplesBeforeTheFinalQuery() {
        String rendered = fewShotService.buildFewShotTemplatePrompt("Trailing query", examples);

        int lastExampleIndex = rendered.indexOf("Category: " + examples.get(examples.size() - 1).label());
        int queryIndex = rendered.indexOf("Message: Trailing query");

        assertThat(lastExampleIndex).isLessThan(queryIndex);
    }

    /** Minimal no-op ChatModel so FewShotService can be constructed without a Spring context or API keys. */
    private static final class NoopChatModel implements org.springframework.ai.chat.model.ChatModel {
        @Override
        public org.springframework.ai.chat.model.ChatResponse call(Prompt prompt) {
            throw new UnsupportedOperationException("not needed for prompt-construction tests");
        }
    }
}
