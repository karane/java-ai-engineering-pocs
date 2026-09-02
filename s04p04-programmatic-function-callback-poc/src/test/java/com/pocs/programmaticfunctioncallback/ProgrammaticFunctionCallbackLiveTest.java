package com.pocs.programmaticfunctioncallback;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class ProgrammaticFunctionCallbackLiveTest {

    @Autowired
    private ProgrammaticFunctionCallbackService service;

    @Test
    void weatherOnlyToolSetAnswersUsingTheWeatherTool() {
        String response = service.askWithWeatherToolOnly("What's the weather in Lisbon? Reply in one sentence.");

        assertThat(response).isNotBlank();
        assertThat(response.toLowerCase()).containsAnyOf("27", "sunny");
    }

    @Test
    void stockOnlyToolSetAnswersUsingTheStockTool() {
        String response = service.askWithStockToolOnly(
                "What's the current share price of ACME? Reply in one sentence.");

        assertThat(response).isNotBlank();
        assertThat(response).contains("128.42");
    }

    @Test
    void dynamicToolSelectionCanExcludeBothTools() {
        // No tools requested - the model must answer (or decline) without calling either function.
        String response = service.askWithDynamicToolSelection("Say hello in one short sentence.", false, false);

        assertThat(response).isNotBlank();
    }

    @Test
    void multiStepReasoningChainProducesFormattedRecommendationFromToolResult() {
        String response = service.recommendPackingForCity("Tokyo");

        assertThat(response).isNotBlank();
        // Tokyo's fake weather is 31C/humid - the follow-up reasoning step should reference the
        // temperature (not just repeat "unknown") and produce packing advice, not raw JSON.
        assertThat(response).contains("31");
        assertThat(response).doesNotContain("{\"city\"");
    }
}
