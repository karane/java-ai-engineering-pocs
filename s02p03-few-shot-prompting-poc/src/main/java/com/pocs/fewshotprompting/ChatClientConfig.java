package com.pocs.fewshotprompting;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    /**\
     * Both Anthropic and Google GenAI auto-configure their own ChatModel bean, so
     * autowiring the generic ChatModel type here would be ambiguous. Depending on
     * the concrete provider type picks out the right one unambiguously, and each
     * ChatClient is exposed under a qualified bean name so FewShotService can
     * contrast the two providers directly.
     */
    @Bean
    public ChatClient anthropicChatClient(AnthropicChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    @Bean
    public ChatClient geminiChatClient(GoogleGenAiChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}
