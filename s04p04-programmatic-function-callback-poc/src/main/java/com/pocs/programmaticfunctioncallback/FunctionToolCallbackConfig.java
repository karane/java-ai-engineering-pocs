package com.pocs.programmaticfunctioncallback;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FunctionToolCallbackConfig {

    @Bean
    public ToolCallback weatherLookupToolCallback() {
        return FunctionToolCallback
                .builder("lookupWeather", FakeDataSource.LOOKUP_WEATHER)
                .description("Looks up the current weather for a city and returns its temperature "
                        + "in Celsius and a short condition description.")
                .inputType(WeatherLookupRequest.class)
                .build();
    }

    @Bean
    public ToolCallback stockPriceToolCallback() {
        return FunctionToolCallback
                .builder("lookupStockPrice", FakeDataSource.LOOKUP_STOCK_PRICE)
                .description("Looks up the current share price for a stock ticker symbol.")
                .inputType(StockPriceRequest.class)
                .build();
    }
}
