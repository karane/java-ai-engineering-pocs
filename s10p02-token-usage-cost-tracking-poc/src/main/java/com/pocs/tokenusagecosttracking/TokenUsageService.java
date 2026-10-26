package com.pocs.tokenusagecosttracking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

/** Makes a chat call, converts its token usage into a cost, and publishes it as a per-provider gauge. */
@Service
public class TokenUsageService {

    private static final String COST_GAUGE_NAME = "gen_ai.client.cost.usd";

    private final Map<Provider, ChatClient> chatClientsByProvider;
    private final MeterRegistry meterRegistry;
    private final Map<Provider, AtomicReference<Double>> lastCostByProvider = new ConcurrentHashMap<>();

    public TokenUsageService(ChatClient anthropicChatClient, ChatClient geminiChatClient,
            MeterRegistry meterRegistry) {
        this.chatClientsByProvider = Map.of(
                Provider.ANTHROPIC, anthropicChatClient,
                Provider.GEMINI, geminiChatClient);
        this.meterRegistry = meterRegistry;
    }

    public TokenUsageResult ask(Provider provider, String question) {
        ChatClient chatClient = chatClientsByProvider.get(provider);
        ChatResponse response = chatClient.prompt().user(question).call().chatResponse();

        String answer = response.getResult().getOutput().getText();
        Usage usage = response.getMetadata().getUsage();
        int promptTokens = usage.getPromptTokens() == null ? 0 : usage.getPromptTokens();
        int completionTokens = usage.getCompletionTokens() == null ? 0 : usage.getCompletionTokens();
        int totalTokens = usage.getTotalTokens() == null ? promptTokens + completionTokens : usage.getTotalTokens();
        double costUsd = CostCalculator.costUsd(provider, usage);

        recordCostGauge(provider, costUsd);

        return new TokenUsageResult(provider, answer, promptTokens, completionTokens, totalTokens, costUsd);
    }

    private void recordCostGauge(Provider provider, double costUsd) {
        AtomicReference<Double> costRef = lastCostByProvider.computeIfAbsent(provider, p -> {
            AtomicReference<Double> ref = new AtomicReference<>(0.0);
            Gauge.builder(COST_GAUGE_NAME, ref, AtomicReference::get)
                    .description("Cost in USD of the most recent chat request, per provider")
                    .tag("provider", p.tag())
                    .register(meterRegistry);
            return ref;
        });
        costRef.set(costUsd);
    }
}
