package com.pocs.programmaticfunctioncallback;

/** Structured result the weather-lookup tool hands back for the model to reason over. */
public record WeatherLookupResponse(String city, int temperatureCelsius, String condition) {
}
