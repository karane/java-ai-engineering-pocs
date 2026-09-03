package com.pocs.restbackedtools;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RestBackedToolsDemoRunner implements CommandLineRunner {

    /** A deliberately non-routable address (TEST-NET-adjacent blackhole) to force a connect timeout. */
    private static final String UNROUTABLE_BASE_URL = "http://10.255.255.1";

    private final ChatClient chatClient;
    private final WeatherService weatherService;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    public RestBackedToolsDemoRunner(ChatClient chatClient, WeatherService weatherService) {
        this.chatClient = chatClient;
        this.weatherService = weatherService;
    }

    @Override
    public void run(String... args) {
        System.out.println("[s04p05] REST-backed tool calling demo (Open-Meteo weather, no API key needed)");

        System.out.println("[s04p05] --- Direct tool call (no LLM) ---");
        WeatherToolResult success = weatherService.getCurrentWeather("Berlin");
        System.out.println("[s04p05] Direct success case -> " + success);

        WeatherToolResult notFound = weatherService.getCurrentWeather("Xyzzzblahnotarealplace");
        System.out.println("[s04p05] Direct error case (unknown city) -> " + notFound);

        WeatherToolResult timedOut = weatherService.getCurrentWeather(
                "Berlin", UNROUTABLE_BASE_URL, UNROUTABLE_BASE_URL);
        System.out.println("[s04p05] Direct timeout case (unroutable host, sandboxed timeout trips) -> "
                + timedOut);

        if (!StringUtils.hasText(anthropicApiKey)) {
            System.out.println("[s04p05] ANTHROPIC_API_KEY not set - skipping LLM + tool round-trip demo.");
            return;
        }

        System.out.println("[s04p05] --- LLM + tool round trip (Claude decides when/how to call the tool) ---");

        String successAnswer = chatClient.prompt()
                .user("What's the current weather in Berlin? Use the weather tool.")
                .tools(weatherService)
                .call()
                .content();
        System.out.println("[s04p05] LLM success round trip -> " + successAnswer);

        String errorAnswer = chatClient.prompt()
                .user("What's the current weather in Xyzzzblahnotarealplace? Use the weather tool and "
                        + "tell me clearly if it fails.")
                .tools(weatherService)
                .call()
                .content();
        System.out.println("[s04p05] LLM error round trip (model reacts to tool error) -> " + errorAnswer);
    }
}
