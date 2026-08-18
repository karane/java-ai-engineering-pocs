package com.pocs.fewshotprompting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class AnthropicFewShotServiceLiveTest {

    private static final String QUERY = "I got billed twice for the same order this month.";

    @Autowired
    private FewShotService fewShotService;

    @Test
    void zeroShotProducesNonEmptyCategory() {
        String category = fewShotService.classifyZeroShotAnthropic(QUERY);
        assertThat(category).isNotBlank();
    }

    @Test
    void fewShotProducesTheExpectedBillingCategory() {
        String category = fewShotService.classifyFewShotAnthropic(QUERY, ClassificationExample.catalog());
        assertThat(category.toLowerCase()).contains("billing");
    }
}
