package com.pocs.promptstuffingcontext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class ContextStuffingServiceLiveTest {

    @Autowired
    private ContextStuffingService contextStuffingService;

    @Autowired
    private ChatClient chatClient;

    @Test
    void stuffedPromptGetsARealAnswerFromClaude() {
        String context = "Spring AI's TokenCountEstimator interface estimates how many "
                + "tokens a piece of text will cost before it is ever sent to a model.";
        ContextStuffingService.StuffedPrompt stuffed =
                contextStuffingService.buildPrompt(context, "What does TokenCountEstimator do?");

        String answer = chatClient.prompt().user(stuffed.renderedPrompt()).call().content();

        assertThat(answer).isNotBlank();
    }
}
