package com.pocs.micrometerobservability;

import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers a {@link LoggingSpanExporter} bean so spans print to the console. */
@Configuration
public class ObservabilityConfig {

    @Bean
    public SpanExporter otelLoggingSpanExporter() {
        return LoggingSpanExporter.create();
    }
}
