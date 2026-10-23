package com.pocs.micrometerobservability;

import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Exercises both provider ChatClient beans and prints a per-provider latency dashboard. */
@Component
public class ObservabilityDemoRunner implements CommandLineRunner {

    private static final String DEMO_QUESTION = "In one short sentence, what is retrieval-augmented generation?";
    private static final String TIMER_NAME = "gen_ai.client.operation";

    private final ChatClient anthropicObservedChatClient;
    private final ChatClient geminiObservedChatClient;
    private final MeterRegistry meterRegistry;

    public ObservabilityDemoRunner(ChatClient anthropicObservedChatClient,
            ChatClient geminiObservedChatClient, MeterRegistry meterRegistry) {
        this.anthropicObservedChatClient = anthropicObservedChatClient;
        this.geminiObservedChatClient = geminiObservedChatClient;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void run(String... args) {
        System.out.println("[s10p01] Question: " + DEMO_QUESTION);

        if (isSet("ANTHROPIC_API_KEY")) {
            System.out.println("[s10p01] --- Claude (Anthropic) ---");
            System.out.println(new ObservabilityChatService(anthropicObservedChatClient).ask(DEMO_QUESTION));
        } else {
            System.out.println("[s10p01] --- Claude (Anthropic) --- skipped, ANTHROPIC_API_KEY not set");
        }

        if (isSet("GEMINI_API_KEY")) {
            System.out.println("[s10p01] --- Gemini ---");
            System.out.println(new ObservabilityChatService(geminiObservedChatClient).ask(DEMO_QUESTION));
        } else {
            System.out.println("[s10p01] --- Gemini --- skipped, GEMINI_API_KEY not set");
        }

        printDashboard();
    }

    private void printDashboard() {
        System.out.println("[s10p01] --- Per-provider latency dashboard (from MeterRegistry) ---");
        boolean any = false;
        for (Meter meter : meterRegistry.getMeters()) {
            if (!TIMER_NAME.equals(meter.getId().getName()) || !(meter instanceof Timer timer)) {
                continue;
            }
            any = true;
            String provider = meter.getId().getTag("gen_ai.system");
            System.out.printf("[s10p01] provider=%s count=%d meanMs=%.1f maxMs=%.1f%n",
                    provider, timer.count(),
                    timer.mean(TimeUnit.MILLISECONDS), timer.max(TimeUnit.MILLISECONDS));
        }
        if (!any) {
            System.out.println("[s10p01] no " + TIMER_NAME + " timer recorded yet (no provider key was set)");
        }
    }

    private boolean isSet(String envVar) {
        String value = System.getenv(envVar);
        return value != null && !value.isBlank();
    }
}
