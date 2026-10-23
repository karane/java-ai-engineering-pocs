package com.pocs.micrometerobservability;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.chat.observation.DefaultChatModelObservationConvention;

/** Extends the default chat observation convention with an extra {@code poc.module} tag. */
public class PocChatModelObservationConvention extends DefaultChatModelObservationConvention {

    @Override
    public KeyValues getLowCardinalityKeyValues(ChatModelObservationContext context) {
        return super.getLowCardinalityKeyValues(context)
                .and(KeyValue.of("poc.module", "s10p01"));
    }
}
