package com.pocs.springaitoolmethod;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Plain Java methods turned into tools purely by annotation. Spring AI derives each tool's JSON schema
 * from the method signature (parameter names/types) plus the {@link ToolParam} descriptions - there is
 * no manual schema-building here, which is the whole point of the {@code @Tool}-annotated-method
 * approach versus the programmatic {@link TimeTools} below.
 *
 * <p>These are intentionally small and deterministic (no external calls, no randomness) so a live test
 * can assert on the model's final answer without flakiness - the interesting part being verified is that
 * the model chooses to call these tools and correctly chains their results, not that arithmetic works.
 */
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
