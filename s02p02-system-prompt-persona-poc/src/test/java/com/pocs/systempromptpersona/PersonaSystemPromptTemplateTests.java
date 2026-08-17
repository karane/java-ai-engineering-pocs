package com.pocs.systempromptpersona;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;

class PersonaSystemPromptTemplateTests {

    @Test
    void systemPromptTemplateRendersRoleAndTonePlaceholders() {
        Message systemMessage = PersonaService.renderPersonaSystemMessage(
                "a terse senior code reviewer", "blunt but professional");

        assertThat(systemMessage.getMessageType()).isEqualTo(MessageType.SYSTEM);
        assertThat(systemMessage.getText())
                .contains("a terse senior code reviewer")
                .contains("blunt but professional")
                .doesNotContain("{role}")
                .doesNotContain("{tone}");
    }

    @Test
    void promptFromMessageListPreservesOrderAndTypes() {
        Message systemMessage = PersonaService.renderPersonaSystemMessage("a pirate", "salty");
        UserMessage userMessage = new UserMessage("What's the weather like?");
        AssistantMessage assistantMessage = new AssistantMessage("Storm's brewin', matey.");
        UserMessage followUp = new UserMessage("Should I sail today?");

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage, assistantMessage, followUp));

        List<Message> instructions = prompt.getInstructions();
        assertThat(instructions).hasSize(4);
        assertThat(instructions.get(0).getMessageType()).isEqualTo(MessageType.SYSTEM);
        assertThat(instructions.get(1).getMessageType()).isEqualTo(MessageType.USER);
        assertThat(instructions.get(2).getMessageType()).isEqualTo(MessageType.ASSISTANT);
        assertThat(instructions.get(3).getMessageType()).isEqualTo(MessageType.USER);
        assertThat(instructions.get(3).getText()).isEqualTo("Should I sail today?");
    }
}
