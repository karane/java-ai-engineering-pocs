package com.pocs.chatoptionsperprovider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class GeminiPitchServiceLiveTest {

    private static final String PRODUCT = "a self-refilling coffee mug";

    @Autowired
    private PitchService pitchService;

    @Test
    void defaultsProduceNonEmptyPitch() {
        String pitch = pitchService.geminiPitchWithDefaults(PRODUCT);
        assertThat(pitch).isNotBlank();
    }

    @Test
    void conservativeOptionsOverrideStillProducesNonEmptyPitch() {
        String pitch = pitchService.geminiPitchWithOptions(PRODUCT, PitchTuning.conservative());
        assertThat(pitch).isNotBlank();
    }

    @Test
    void creativeOptionsOverrideStillProducesNonEmptyPitch() {
        String pitch = pitchService.geminiPitchWithOptions(PRODUCT, PitchTuning.creative());
        assertThat(pitch).isNotBlank();
    }

    @Test
    void veryLowMaxTokensTruncatesOutputToAFewWords() {
        PitchTuning tightBudget = new PitchTuning(0.5, 0.9, 20, 8);
        String pitch = pitchService.geminiPitchWithOptions(PRODUCT, tightBudget);
        assertThat(pitch).isNotBlank();
        assertThat(pitch.split("\\s+").length).isLessThanOrEqualTo(15);
    }
}
