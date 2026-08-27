package com.pocs.springaitoolmethod;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class ToolMethodLiveTest {

    @Autowired
    private ToolCallingService toolCallingService;

    @Test
    void modelChainsCalculatorToolCallsToReachTheCorrectAnswer() {
        String answer = toolCallingService.askAnthropic(
                "Add 12.5 and 7.3, then multiply the sum by 3. Reply with only the final number.");

        assertThat(answer).isNotBlank();
        // (12.5 + 7.3) * 3 = 59.4 - only reachable by actually calling both tools and chaining results.
        assertThat(answer).contains("59.4");
    }

    @Test
    void modelUsesTheProgrammaticallyBuiltTimeTool() {
        String answer = toolCallingService.askAnthropic(
                "What year is it right now, according to the current date/time tool? Reply with only "
                        + "the 4-digit year.");

        assertThat(answer).isNotBlank();
        assertThat(answer).matches("(?s).*\\b20\\d{2}\\b.*");
    }
}
