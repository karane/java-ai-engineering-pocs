package com.pocs.systempromptpersona;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Demonstrates SystemPromptTemplate-driven persona construction and explicit
 * Message-list Prompt building against both providers, so their system-prompt
 * handling can be compared directly rather than through a provider-blind facade.
 */
@Service
public class PersonaService {

    private static final String PERSONA_TEMPLATE = """
            You are {role}. Stay strictly in character.
            Reply in a {tone} tone. Keep every answer to two sentences or fewer.
            Never break character to explain that you are an AI language model.
            """;

    @Autowired
    @Qualifier("claudeChatClient")
    private ChatClient claudeChatClient;

    @Autowired
    @Qualifier("geminiChatClient")
    private ChatClient geminiChatClient;

    static Message renderPersonaSystemMessage(String role, String tone) {
        SystemPromptTemplate template = new SystemPromptTemplate(PERSONA_TEMPLATE);
        return template.createMessage(Map.of("role", role, "tone", tone));
    }

    public String claudePersonaAnswer(String role, String tone, String question) {
        return ask(claudeChatClient, role, tone, question);
    }

    public String geminiPersonaAnswer(String role, String tone, String question) {
        return ask(geminiChatClient, role, tone, question);
    }

    private String ask(ChatClient chatClient, String role, String tone, String question) {
        Message systemMessage = renderPersonaSystemMessage(role, tone);
        UserMessage userMessage = new UserMessage(question);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        return chatClient.prompt(prompt).call().content();
    }

    public String claudeMultiTurn(String role, String tone, String priorUserTurn,
                                   String priorAssistantTurn, String finalQuestion) {
        return multiTurn(claudeChatClient, role, tone, priorUserTurn, priorAssistantTurn, finalQuestion);
    }

    public String geminiMultiTurn(String role, String tone, String priorUserTurn,
                                   String priorAssistantTurn, String finalQuestion) {
        return multiTurn(geminiChatClient, role, tone, priorUserTurn, priorAssistantTurn, finalQuestion);
    }

    private String multiTurn(ChatClient chatClient, String role, String tone, String priorUserTurn,
                              String priorAssistantTurn, String finalQuestion) {
        List<Message> messages = List.of(
                renderPersonaSystemMessage(role, tone), // history/memory
                new UserMessage(priorUserTurn), // history/memory
                new AssistantMessage(priorAssistantTurn), // history/memory
                new UserMessage(finalQuestion) // last interaction, LLM will answer to it next
        );
        return chatClient.prompt(new Prompt(messages)).call().content();
    }

    public PersonaComparison compare(String role, String tone, String question) {
        String claudeAnswer = claudePersonaAnswer(role, tone, question);
        String geminiAnswer = geminiPersonaAnswer(role, tone, question);
        return new PersonaComparison(claudeAnswer, geminiAnswer);
    }

    public record PersonaComparison(String claudeAnswer, String geminiAnswer) {
    }
}
