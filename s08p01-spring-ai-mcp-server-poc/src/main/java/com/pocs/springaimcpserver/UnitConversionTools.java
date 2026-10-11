package com.pocs.springaimcpserver;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/** Plain @Tool-annotated Spring bean exposed as MCP tools via {@link ToolCallbackProviderConfig}. */
@Component
public class UnitConversionTools {

    @Tool(description = "Convert a temperature reading from degrees Celsius to degrees Fahrenheit")
    public double celsiusToFahrenheit(
            @ToolParam(description = "The temperature in degrees Celsius") double celsius) {
        return celsius * 9.0 / 5.0 + 32.0;
    }

    @Tool(description = "Convert a distance in kilometers to miles")
    public double kilometersToMiles(
            @ToolParam(description = "The distance in kilometers") double kilometers) {
        return kilometers * 0.621371;
    }

    @Tool(description = "Convert a weight in kilograms to pounds")
    public double kilogramsToPounds(
            @ToolParam(description = "The weight in kilograms") double kilograms) {
        return kilograms * 2.20462;
    }
}
