package com.pocs.restbackedtools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class WeatherService {

    private static final int ERROR_LOCATION_NOT_FOUND = 1;
    private static final int ERROR_UPSTREAM_HTTP = 2;
    private static final int ERROR_NETWORK = 3;
    private static final int ERROR_TIMEOUT = 4;

    private final RestClient restClient;
    private final String forecastBaseUrl;
    private final String geocodingBaseUrl;

    public WeatherService(
            RestClient weatherRestClient,
            @Value("${weather.api.base-url}") String forecastBaseUrl,
            @Value("${weather.api.geocoding-base-url}") String geocodingBaseUrl) {
        this.restClient = weatherRestClient;
        this.forecastBaseUrl = forecastBaseUrl;
        this.geocodingBaseUrl = geocodingBaseUrl;
    }

    @Tool(name = "get_current_weather", description = "Get the current temperature (Celsius) and wind "
            + "speed (km/h) for a named city, using the free Open-Meteo API. Returns a JSON-RPC-style "
            + "envelope: either a populated \"result\" on success, or a non-null \"error\" (with a code "
            + "and message) if the city can't be found or the upstream API call fails - check \"error\" "
            + "before reading \"result\".")
    public WeatherToolResult getCurrentWeather(
            @ToolParam(description = "The city name to look up, e.g. \"Berlin\" or \"Tokyo\"") String city) {
        return getCurrentWeather(city, forecastBaseUrl, geocodingBaseUrl);
    }

    WeatherToolResult getCurrentWeather(String city, String forecastBase, String geocodingBase) {
        Geocode geocode;
        try {
            geocode = geocode(city, geocodingBase);
        } catch (RestClientResponseException e) {
            return WeatherToolResult.failure(ERROR_UPSTREAM_HTTP,
                    "Geocoding API returned HTTP " + e.getStatusCode().value() + " for city \"" + city + "\".");
        } catch (ResourceAccessException e) {
            return timeoutOrNetworkFailure(e, "geocoding");
        }

        if (geocode == null) {
            return WeatherToolResult.failure(ERROR_LOCATION_NOT_FOUND,
                    "No location found matching \"" + city + "\". Check the spelling or try a nearby "
                            + "larger city.");
        }

        try {
            Map<String, Object> forecast = restClient.get()
                    .uri(forecastBase + "/v1/forecast?latitude={lat}&longitude={lon}"
                            + "&current=temperature_2m,wind_speed_10m", geocode.latitude(), geocode.longitude())
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                    });

            Object current = forecast == null ? null : forecast.get("current");
            if (current == null) {
                return WeatherToolResult.failure(ERROR_UPSTREAM_HTTP,
                        "Forecast API response for \"" + city + "\" had no \"current\" weather block.");
            }

            return WeatherToolResult.success(Map.of(
                    "city", geocode.name(),
                    "country", geocode.country(),
                    "latitude", geocode.latitude(),
                    "longitude", geocode.longitude(),
                    "current", current));
        } catch (RestClientResponseException e) {
            return WeatherToolResult.failure(ERROR_UPSTREAM_HTTP,
                    "Forecast API returned HTTP " + e.getStatusCode().value() + " for city \"" + city + "\".");
        } catch (ResourceAccessException e) {
            return timeoutOrNetworkFailure(e, "forecast");
        }
    }

    private WeatherToolResult timeoutOrNetworkFailure(ResourceAccessException e, String stage) {
        String message = String.valueOf(e.getMessage());
        boolean looksLikeTimeout = message.toLowerCase().contains("timeout")
                || message.toLowerCase().contains("timed out");
        if (looksLikeTimeout) {
            return WeatherToolResult.failure(ERROR_TIMEOUT,
                    "Timed out calling the " + stage + " API - the upstream service didn't respond in "
                            + "time. Try again shortly.");
        }
        return WeatherToolResult.failure(ERROR_NETWORK,
                "Network error calling the " + stage + " API: " + message);
    }

    @SuppressWarnings("unchecked")
    private Geocode geocode(String city, String geocodingBase) {
        Map<String, Object> response = restClient.get()
                .uri(geocodingBase + "/v1/search?name={name}&count=1", city)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                });

        if (response == null || !(response.get("results") instanceof List<?> results) || results.isEmpty()) {
            return null;
        }

        Map<String, Object> first = (Map<String, Object>) results.get(0);
        return new Geocode(
                String.valueOf(first.get("name")),
                String.valueOf(first.get("country")),
                ((Number) first.get("latitude")).doubleValue(),
                ((Number) first.get("longitude")).doubleValue());
    }

    private record Geocode(String name, String country, double latitude, double longitude) {
    }
}
