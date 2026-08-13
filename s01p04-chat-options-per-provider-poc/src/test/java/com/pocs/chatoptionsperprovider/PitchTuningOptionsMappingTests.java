package com.pocs.chatoptionsperprovider;

import org.junit.jupiter.api.Test;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;

import static org.assertj.core.api.Assertions.assertThat;

class PitchTuningOptionsMappingTests {

    @Test
    void anthropicOptionsCarryTemperatureAndMaxTokens() {
        PitchTuning tuning = PitchTuning.creative();

        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .temperature(tuning.creativity())
                .maxTokens(tuning.maxTokens())
                .build();

        assertThat(options.getTemperature()).isEqualTo(tuning.creativity());
        assertThat(options.getMaxTokens()).isEqualTo(tuning.maxTokens());
    }

    @Test
    void anthropicOptionsCarryTopPAndMaxTokensAsAnAlternativeToTemperature() {
        // Claude's API rejects a request that sets both temperature and topP at
        // once, so the two are mutually exclusive per call - see PitchService.
        PitchTuning tuning = PitchTuning.conservative();

        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .topP(tuning.topP())
                .maxTokens(tuning.maxTokens())
                .build();

        assertThat(options.getTopP()).isEqualTo(tuning.topP());
        assertThat(options.getTemperature()).isNull();
        assertThat(options.getMaxTokens()).isEqualTo(tuning.maxTokens());
    }

    @Test
    void geminiOptionsCarryTemperatureTopKAndCandidateCount() {
        PitchTuning tuning = PitchTuning.conservative();

        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .temperature(tuning.creativity())
                .topK(tuning.topK())
                .candidateCount(1)
                .maxOutputTokens(tuning.maxTokens())
                .build();

        assertThat(options.getTemperature()).isEqualTo(tuning.creativity());
        assertThat(options.getTopK()).isEqualTo(tuning.topK());
        assertThat(options.getCandidateCount()).isEqualTo(1);
        assertThat(options.getMaxTokens()).isEqualTo(tuning.maxTokens());
    }

    @Test
    void bothProviderOptionTypesImplementTheSharedChatOptionsInterface() {
        ChatOptions anthropicOptions = AnthropicChatOptions.builder().temperature(0.5).build();
        ChatOptions geminiOptions = GoogleGenAiChatOptions.builder().temperature(0.5).build();

        assertThat(anthropicOptions).isInstanceOf(ChatOptions.class);
        assertThat(geminiOptions).isInstanceOf(ChatOptions.class);
        assertThat(anthropicOptions.getTemperature()).isEqualTo(geminiOptions.getTemperature());
    }

    @Test
    void creativeTuningHasHigherTemperatureAndTokenBudgetThanConservative() {
        PitchTuning conservative = PitchTuning.conservative();
        PitchTuning creative = PitchTuning.creative();

        assertThat(creative.creativity()).isGreaterThan(conservative.creativity());
        assertThat(creative.topP()).isGreaterThan(conservative.topP());
        assertThat(creative.topK()).isGreaterThan(conservative.topK());
        assertThat(creative.maxTokens()).isGreaterThan(conservative.maxTokens());
    }

    @Test
    void anthropicOptionsWithoutOverrideLeaveMaxTokensNull() {
        AnthropicChatOptions options = AnthropicChatOptions.builder().temperature(0.3).build();

        assertThat(options.getMaxTokens()).isNull();
        assertThat(options.getTemperature()).isEqualTo(0.3);
    }

    @Test
    void geminiOptionsWithoutOverrideLeaveTopKNull() {
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder().temperature(0.3).build();

        assertThat(options.getTopK()).isNull();
        assertThat(options.getTemperature()).isEqualTo(0.3);
    }
}
