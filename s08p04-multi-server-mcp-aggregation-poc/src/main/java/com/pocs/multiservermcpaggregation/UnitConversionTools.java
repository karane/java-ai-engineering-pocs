package com.pocs.multiservermcpaggregation;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/** Server A's tools: unit conversion. Active only under the "server-a" profile (see ServerAConfig). */
@Component
public class UnitConversionTools {

    @Tool(description = "Convert a temperature reading from degrees Celsius to degrees Fahrenheit")
    public double celsiusToFahrenheit(
            @ToolParam(description = "The temperature in degrees Celsius") double celsius) {
        return celsius * 9.0 / 5.0 + 32.0;
    }

    @Tool(description = "[server-a] Echo a diagnostic ping, identifying which server answered")
    public String ping() {
        return "pong from server-a (unit-conversion-server)";
    }
}
