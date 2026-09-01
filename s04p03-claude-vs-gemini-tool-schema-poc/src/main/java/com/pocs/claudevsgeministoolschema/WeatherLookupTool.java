package com.pocs.claudevsgeministoolschema;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WeatherLookupTool {

    @Tool(name = "get_weather", description = "Get the current weather conditions for a named city")
    public String getWeather(
            @ToolParam(description = "The city name to look up weather for, e.g. 'Paris' or 'Tokyo'") String city,
            @ToolParam(description = "Temperature unit to report in: 'celsius' or 'fahrenheit'", required = false)
            String unit) {
        String resolvedUnit = StringUtils.hasText(unit) ? unit : "celsius";
        int fakeTemp = Math.floorMod(city.hashCode(), 30) + 5;
        return city + ": " + fakeTemp + " degrees " + resolvedUnit + ", partly cloudy";
    }
}
