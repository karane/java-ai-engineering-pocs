package com.pocs.programmaticfunctioncallback;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

@Service
public class ProgrammaticFunctionCallbackService {

    private final ChatClient chatClient;
    private final ToolCallback weatherLookupToolCallback;
    private final ToolCallback stockPriceToolCallback;

    public ProgrammaticFunctionCallbackService(
            ChatClient chatClient,
            ToolCallback weatherLookupToolCallback,
            ToolCallback stockPriceToolCallback) {
        this.chatClient = chatClient;
        this.weatherLookupToolCallback = weatherLookupToolCallback;
        this.stockPriceToolCallback = stockPriceToolCallback;
    }

    /** Only the weather tool is on the table for this request. */
    public String askWithWeatherToolOnly(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .toolCallbacks(weatherLookupToolCallback)
                .call()
                .content();
    }

    /** Only the stock-price tool is on the table for this request - a different tool set, same ChatClient bean. */
    public String askWithStockToolOnly(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .toolCallbacks(stockPriceToolCallback)
                .call()
                .content();
    }

    public String askWithDynamicToolSelection(String userMessage, boolean includeWeather, boolean includeStock) {
        ChatClient.ChatClientRequestSpec request = chatClient.prompt().user(userMessage);

        if (includeWeather && includeStock) {
            request = request.toolCallbacks(weatherLookupToolCallback, stockPriceToolCallback);
        } else if (includeWeather) {
            request = request.toolCallbacks(weatherLookupToolCallback);
        } else if (includeStock) {
            request = request.toolCallbacks(stockPriceToolCallback);
        }

        return request.call().content();
    }

    public String recommendPackingForCity(String city) {
        String instructions = """
                Look up the current weather for %s using the weather tool. Then, using the returned \
                temperature and condition, write exactly two sentences: first state the temperature \
                in Celsius and condition, then recommend what to pack based on those specific values \
                (e.g. an umbrella for rain, sunscreen for high heat and sun, a warm layer for cold).\
                """.formatted(city);

        return chatClient.prompt()
                .user(instructions)
                .toolCallbacks(weatherLookupToolCallback)
                .call()
                .content();
    }
}
