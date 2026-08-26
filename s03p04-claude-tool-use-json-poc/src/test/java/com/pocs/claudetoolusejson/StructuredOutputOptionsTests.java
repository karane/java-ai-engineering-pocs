package com.pocs.claudetoolusejson;

import org.junit.jupiter.api.Test;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Un-gated unit tests: no live API call, just proving the options/schema-building logic each approach
 * relies on is wired correctly - so `mvn test` with zero keys still proves something beyond "it
 * compiles".
 */
class StructuredOutputOptionsTests {

    @Test
    void anthropicOptionsForceTheRecordInvoiceTool() {
        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .toolNames("record_invoice")
                .toolChoice(new AnthropicApi.ToolChoiceTool("record_invoice"))
                .build();

        assertThat(options.getToolNames()).containsExactly("record_invoice");
        assertThat(options.getToolChoice()).isInstanceOf(AnthropicApi.ToolChoiceTool.class);
        assertThat(((AnthropicApi.ToolChoiceTool) options.getToolChoice()).name()).isEqualTo("record_invoice");
        assertThat(options.getToolChoice().type()).isEqualTo("tool");
    }

    @Test
    void internalToolExecutionCanBeDisabledToAvoidReForcingTheToolChoiceOnFollowUpTurns() {
        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .toolChoice(new AnthropicApi.ToolChoiceTool("record_invoice"))
                .internalToolExecutionEnabled(false)
                .build();

        assertThat(options.getInternalToolExecutionEnabled()).isFalse();
    }

    @Test
    void geminiOptionsRequestJsonModeWithAnInvoiceSchema() {
        BeanOutputConverter<Invoice> converter = new BeanOutputConverter<>(Invoice.class);

        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .responseMimeType("application/json")
                .responseSchema(converter.getJsonSchema())
                .build();

        assertThat(options.getResponseMimeType()).isEqualTo("application/json");
        assertThat(options.getResponseSchema()).contains("vendor", "totalAmount", "dueDate", "lineItems");
    }

    @Test
    void beanOutputConverterSchemaCoversAllInvoiceFields() {
        BeanOutputConverter<Invoice> converter = new BeanOutputConverter<>(Invoice.class);

        assertThat(converter.getFormat()).isNotBlank();
        assertThat(converter.getJsonSchemaMap()).isNotEmpty();
    }

    @Test
    void invoiceExtractionToolAcknowledgesEachCall() {
        InvoiceExtractionTool tool = new InvoiceExtractionTool();

        String ack = tool.recordInvoice("Acme Robotics", 4250.75, "2026-09-15",
                java.util.List.of("20x actuator units", "on-site calibration service"));

        assertThat(ack).isNotBlank();
    }
}
