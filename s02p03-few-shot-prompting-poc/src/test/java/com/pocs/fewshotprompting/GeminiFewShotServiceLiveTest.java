package com.pocs.fewshotprompting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class GeminiFewShotServiceLiveTest {

    private static final String QUERY = "I got billed twice for the same order this month.";

    @Autowired
    private FewShotService fewShotService;

    @Test
    void zeroShotProducesNonEmptyCategory() {
        String category = fewShotService.classifyZeroShotGemini(QUERY);
        assertThat(category).isNotBlank();
    }

    @Test
    void fewShotProducesTheExpectedBillingCategory() {
        String category = fewShotService.classifyFewShotGemini(QUERY, ClassificationExample.catalog());
        assertThat(category.toLowerCase()).contains("billing");
    }
}
