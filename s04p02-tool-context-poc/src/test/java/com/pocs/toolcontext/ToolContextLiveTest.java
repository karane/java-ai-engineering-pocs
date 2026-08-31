package com.pocs.toolcontext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class ToolContextLiveTest {

    private static final String QUESTION = "What tier is my account, and what currency should I quote "
            + "pricing in for the 'Orbital Widget'?";

    @Autowired
    private ToolContextService toolContextService;

    @Test
    void automaticToolLoopPersonalizesAnswerFromToolContext() {
        String answer = toolContextService.automaticToolLoop("user-42", QUESTION);

        assertThat(answer).isNotBlank();
        assertThat(answer).containsIgnoringCase("gold");
        assertThat(answer).containsIgnoringCase("eur");
    }

    @Test
    void manualToolLoopPersonalizesAnswerFromToolContext() {
        String answer = toolContextService.manualToolLoop("user-7", QUESTION);

        assertThat(answer).isNotBlank();
        assertThat(answer).containsIgnoringCase("standard");
        assertThat(answer).containsIgnoringCase("usd");
    }
}
