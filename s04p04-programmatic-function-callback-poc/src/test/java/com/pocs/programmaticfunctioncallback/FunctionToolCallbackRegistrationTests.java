package com.pocs.programmaticfunctioncallback;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionToolCallbackRegistrationTests {

    @Test
    void weatherLookupFunctionProducesExpectedResult() {
        WeatherLookupResponse response = FakeDataSource.LOOKUP_WEATHER.apply(new WeatherLookupRequest("Lisbon"));

        assertThat(response.city()).isEqualTo("Lisbon");
        assertThat(response.temperatureCelsius()).isEqualTo(27);
        assertThat(response.condition()).isEqualTo("sunny");
    }

    @Test
    void stockPriceFunctionProducesExpectedResult() {
        StockPriceResponse response = FakeDataSource.LOOKUP_STOCK_PRICE.apply(new StockPriceRequest("acme"));

        assertThat(response.ticker()).isEqualTo("ACME");
        assertThat(response.price()).isEqualTo(128.42);
    }

    @Test
    void weatherToolCallbackIsBuiltFromPlainFunctionWithoutAnnotations() {
        ToolCallback callback = FunctionToolCallback
                .builder("lookupWeather", FakeDataSource.LOOKUP_WEATHER)
                .description("Looks up the current weather for a city.")
                .inputType(WeatherLookupRequest.class)
                .build();

        assertThat(callback.getToolDefinition().name()).isEqualTo("lookupWeather");
        assertThat(callback.getToolDefinition().description())
                .isEqualTo("Looks up the current weather for a city.");
        // The input schema is generated from the plain record's fields - no @Tool/@ToolParam
        // annotations were used anywhere to produce it.
        assertThat(callback.getToolDefinition().inputSchema()).contains("city");
    }

    @Test
    void invokingTheToolCallbackEndToEndReturnsJsonWithFieldsFromTheFunctionResult() {
        ToolCallback callback = FunctionToolCallback
                .builder("lookupStockPrice", FakeDataSource.LOOKUP_STOCK_PRICE)
                .description("Looks up the current share price for a stock ticker symbol.")
                .inputType(StockPriceRequest.class)
                .build();

        String result = callback.call("{\"ticker\":\"initech\"}");

        assertThat(result).contains("INITECH");
        assertThat(result).contains("9.87");
    }

    @Test
    void differentToolCallbacksExposeDifferentNamesForDynamicSelection() {
        ToolCallback weather = FunctionToolCallback
                .builder("lookupWeather", FakeDataSource.LOOKUP_WEATHER)
                .inputType(WeatherLookupRequest.class)
                .build();
        ToolCallback stock = FunctionToolCallback
                .builder("lookupStockPrice", FakeDataSource.LOOKUP_STOCK_PRICE)
                .inputType(StockPriceRequest.class)
                .build();

        assertThat(weather.getToolDefinition().name()).isNotEqualTo(stock.getToolDefinition().name());
    }
}
