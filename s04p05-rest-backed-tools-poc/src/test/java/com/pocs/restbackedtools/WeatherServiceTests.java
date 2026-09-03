package com.pocs.restbackedtools;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WeatherServiceTests {

    private static final String UNROUTABLE_BASE_URL = "http://10.255.255.1";

    @Autowired
    private WeatherService weatherService;

    @Test
    void successCaseReturnsWeatherForKnownCity() {
        WeatherToolResult result = weatherService.getCurrentWeather("Berlin");

        assertThat(result.isError()).isFalse();
        assertThat(result.result()).isNotNull();
    }

    @Test
    void errorCasePropagatesStructuredErrorForUnknownCity() {
        WeatherToolResult result = weatherService.getCurrentWeather("Xyzzzblahnotarealplace");

        assertThat(result.isError()).isTrue();
        assertThat(result.result()).isNull();
        assertThat(result.error().code()).isEqualTo(1);
        assertThat(result.error().message()).containsIgnoringCase("no location found");
    }

    @Test
    void timeoutCasePropagatesStructuredErrorInsteadOfHanging() {
        long start = System.currentTimeMillis();
        WeatherToolResult result = weatherService.getCurrentWeather(
                "Berlin", UNROUTABLE_BASE_URL, UNROUTABLE_BASE_URL);
        long elapsedMs = System.currentTimeMillis() - start;

        assertThat(result.isError()).isTrue();
        assertThat(elapsedMs).isLessThan(20_000);
    }
}
