package com.pocs.prompttemplate;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class PromptTemplateServiceLiveTest {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private PromptTemplateService promptTemplateService;

    @Test
    void renderedInlineTemplateCanBeSentToClaude() {
        String rendered = promptTemplateService.renderInlineTemplate(
                "Reply with exactly one word: {word}", Map.of("word", "pong"));

        String answer = chatClient.prompt().user(rendered).call().content();

        assertThat(answer).isNotBlank();
    }

    @Test
    void renderedResourceTemplateCanBeSentToClaude() {
        String rendered = promptTemplateService.renderGreetingFromResource(
                Map.of("name", "Grace", "company", "Anthropic"));

        String answer = chatClient.prompt().user(rendered).call().content();

        assertThat(answer).isNotBlank();
    }
}
