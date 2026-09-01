package com.pocs.claudevsgeministoolschema;

import org.junit.jupiter.api.Test;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.tool.definition.ToolDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class ToolSchemaGenerationTests {

    private final ToolSchemaService toolSchemaService =
            new ToolSchemaService(null, null, new WeatherLookupTool());

    @Test
    void sharedToolDefinitionCapturesTheAnnotatedMethodShape() {
        ToolDefinition definition = toolSchemaService.sharedToolDefinition();

        assertThat(definition.name()).isEqualTo("get_weather");
        assertThat(definition.description()).contains("weather");
        assertThat(definition.inputSchema()).contains("city", "unit");
    }

    @Test
    void anthropicToolSchemaJsonWrapsTheSharedSchemaInAnAnthropicToolShape() throws Exception {
        String json = toolSchemaService.anthropicToolSchemaJson();

        assertThat(json).contains("\"name\"", "get_weather", "input_schema", "city");
    }

    @Test
    void geminiFunctionDeclarationJsonWrapsTheSameSharedSchemaInAGeminiFunctionDeclarationShape() throws Exception {
        String json = toolSchemaService.geminiFunctionDeclarationJson();

        assertThat(json).contains("get_weather", "city");
    }

    @Test
    void anthropicOptionsCanForceTheGetWeatherTool() {
        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .toolNames("get_weather")
                .toolChoice(new AnthropicApi.ToolChoiceTool("get_weather"))
                .build();

        assertThat(options.getToolNames()).containsExactly("get_weather");
        assertThat(options.getToolChoice()).isInstanceOf(AnthropicApi.ToolChoiceTool.class);
        assertThat(options.getToolChoice().type()).isEqualTo("tool");
    }

    @Test
    void anthropicToolChoiceAnyAndAutoAreBothAvailable() {
        AnthropicChatOptions anyOptions = AnthropicChatOptions.builder()
                .toolChoice(new AnthropicApi.ToolChoiceAny())
                .build();
        AnthropicChatOptions autoOptions = AnthropicChatOptions.builder()
                .toolChoice(new AnthropicApi.ToolChoiceAuto())
                .build();

        assertThat(anyOptions.getToolChoice().type()).isEqualTo("any");
        assertThat(autoOptions.getToolChoice().type()).isEqualTo("auto");
    }

    @Test
    void geminiOptionsOnlyExposeToolNameRestrictionNotAForcedToolChoice() {
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .toolName("get_weather")
                .build();

        // GoogleGenAiChatOptions has no toolChoice-equivalent field in Spring AI 1.1.8 - toolNames is
        // the only tool-selection knob it exposes, and it restricts rather than forces. See README/BLOG
        // Gotcha section for the underlying Google GenAI SDK's FunctionCallingConfigMode that Spring AI
        // does not currently surface here.
        assertThat(options.getToolNames()).containsExactly("get_weather");
    }

    @Test
    void weatherLookupToolReturnsAReading() {
        WeatherLookupTool tool = new WeatherLookupTool();

        String reading = tool.getWeather("Paris", "celsius");

        assertThat(reading).contains("Paris").contains("celsius");
    }
}
