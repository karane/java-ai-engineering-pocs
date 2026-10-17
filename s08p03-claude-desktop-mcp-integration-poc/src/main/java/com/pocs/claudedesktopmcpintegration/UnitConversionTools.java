package com.pocs.claudedesktopmcpintegration;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Same tool shape as s08p01's UnitConversionTools, duplicated here (rather than depending on that
 * module) so this POC's server jar is self-contained - exactly the jar a human points
 * claude_desktop_config.json at, with no other module needed on disk.
 */
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
