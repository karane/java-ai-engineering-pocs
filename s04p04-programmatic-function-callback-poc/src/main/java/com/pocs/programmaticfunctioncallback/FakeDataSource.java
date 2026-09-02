package com.pocs.programmaticfunctioncallback;

import java.util.Map;
import java.util.function.Function;

public final class FakeDataSource {

    private static final Map<String, WeatherLookupResponse> WEATHER = Map.of(
            "lisbon", new WeatherLookupResponse("Lisbon", 27, "sunny"),
            "berlin", new WeatherLookupResponse("Berlin", 14, "overcast"),
            "tokyo", new WeatherLookupResponse("Tokyo", 31, "humid"));

    private static final Map<String, StockPriceResponse> STOCKS = Map.of(
            "acme", new StockPriceResponse("ACME", 128.42, "USD"),
            "globex", new StockPriceResponse("GLOBEX", 57.10, "USD"),
            "initech", new StockPriceResponse("INITECH", 9.87, "USD"));

    private FakeDataSource() {
    }

    public static final Function<WeatherLookupRequest, WeatherLookupResponse> LOOKUP_WEATHER = request -> {
        String key = request.city() == null ? "" : request.city().trim().toLowerCase();
        return WEATHER.getOrDefault(key, new WeatherLookupResponse(request.city(), 20, "unknown"));
    };

    public static final Function<StockPriceRequest, StockPriceResponse> LOOKUP_STOCK_PRICE = request -> {
        String key = request.ticker() == null ? "" : request.ticker().trim().toLowerCase();
        return STOCKS.getOrDefault(key, new StockPriceResponse(request.ticker(), 0.0, "USD"));
    };
}
