package com.pocs.springaitoolmethod;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTools {

    @Tool(description = "Add two numbers together and return the sum")
    public double add(
            @ToolParam(description = "The first number") double a,
            @ToolParam(description = "The second number") double b) {
        return a + b;
    }

    @Tool(description = "Multiply two numbers together and return the product")
    public double multiply(
            @ToolParam(description = "The first number") double a,
            @ToolParam(description = "The second number") double b) {
        return a * b;
    }

    @Tool(description = "Convert a temperature reading from degrees Celsius to degrees Fahrenheit")
    public double celsiusToFahrenheit(
            @ToolParam(description = "The temperature in degrees Celsius") double celsius) {
        return celsius * 9.0 / 5.0 + 32.0;
    }
}
