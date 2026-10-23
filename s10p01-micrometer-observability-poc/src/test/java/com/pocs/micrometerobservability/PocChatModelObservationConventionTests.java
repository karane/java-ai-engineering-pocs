package com.pocs.micrometerobservability;

import io.micrometer.common.KeyValue;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit test for the custom {@code ChatModelObservationConvention} extension point. */
class PocChatModelObservationConventionTests {

    @Test
    void addsPocModuleTagOnTopOfDefaultConventionTags() {
        PocChatModelObservationConvention convention = new PocChatModelObservationConvention();
        ChatOptions options = ChatOptions.builder().model("claude-haiku-4-5-20251001").build();
        ChatModelObservationContext context = ChatModelObservationContext.builder()
                .prompt(new Prompt("does retrieval-augmented generation need an extra tag?", options))
                .provider("anthropic")
                .build();

        var tags = convention.getLowCardinalityKeyValues(context);

        assertThat(tags).contains(KeyValue.of("poc.module", "s10p01"));
        // The default convention's own ai-provider tag should still be present untouched.
        assertThat(tags.stream().anyMatch(tag -> "anthropic".equals(tag.getValue()))).isTrue();
    }

    @Test
    void conventionNameIsUnchangedFromTheDefault() {
        PocChatModelObservationConvention convention = new PocChatModelObservationConvention();
        assertThat(convention.getName()).isEqualTo("gen_ai.client.operation");
    }
}
