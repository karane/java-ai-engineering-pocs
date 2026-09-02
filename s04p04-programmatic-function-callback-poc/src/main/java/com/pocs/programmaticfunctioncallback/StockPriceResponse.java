package com.pocs.programmaticfunctioncallback;

/** Structured result the stock-price tool hands back for the model to reason over. */
public record StockPriceResponse(String ticker, double price, String currency) {
}
