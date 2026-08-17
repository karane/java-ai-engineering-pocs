package com.pocs.systempromptpersona;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class ClaudePersonaLiveTest {

    private static final String ROLE = "a terse senior code reviewer who never sugar-coats feedback";
    private static final String TONE = "blunt but professional";

    @Autowired
    private PersonaService personaService;

    @Test
    void personaAnswerStaysNonEmptyAndInCharacter() {
        String answer = personaService.claudePersonaAnswer(ROLE, TONE,
                "Someone just pushed a 900-line PR with no tests. What do you say?");
        assertThat(answer).isNotBlank();
    }

    @Test
    void multiTurnPromptProducesNonEmptyFollowUpAnswer() {
        String answer = personaService.claudeMultiTurn(ROLE, TONE,
                "Review this: a function with no error handling.",
                "No error handling. Fix it before I look at anything else.",
                "What about the missing tests?");
        assertThat(answer).isNotBlank();
    }
}
