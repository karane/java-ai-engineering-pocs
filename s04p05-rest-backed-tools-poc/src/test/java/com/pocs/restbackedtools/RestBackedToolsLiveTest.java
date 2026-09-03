package com.pocs.restbackedtools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class RestBackedToolsLiveTest {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private WeatherService weatherService;

    @Test
    void modelCallsToolAndReportsWeatherForKnownCity() {
        String answer = chatClient.prompt()
                .user("What's the current weather in Berlin right now? Use the weather tool.")
                .tools(weatherService)
                .call()
                .content();

        assertThat(answer).isNotBlank();
    }

    @Test
    void modelReactsToStructuredToolErrorForUnknownCity() {
        String answer = chatClient.prompt()
                .user("What's the current weather in Xyzzzblahnotarealplace? Use the weather tool and "
                        + "explain clearly if it can't be found.")
                .tools(weatherService)
                .call()
                .content();

        assertThat(answer).isNotBlank();
    }
}
