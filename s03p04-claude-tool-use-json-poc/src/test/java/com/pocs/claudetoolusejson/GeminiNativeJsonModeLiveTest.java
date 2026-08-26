package com.pocs.claudetoolusejson;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises approach 2 (Gemini native JSON mode) and approach 3 (prompt-engineered JSON) against the
 * live Gemini API, gated so `mvn test` with no keys never attempts a real call.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class GeminiNativeJsonModeLiveTest {

    private static final String SAMPLE_INVOICE_TEXT =
            "Acme Robotics billed us $4,250.75, due on 2026-09-15, for two line items: "
                    + "\"20x actuator units\" and \"on-site calibration service\".";

    @Autowired
    private StructuredOutputService structuredOutputService;

    @Test
    void nativeJsonModeExtractsInvoiceFields() {
        Invoice invoice = structuredOutputService.geminiNativeJsonMode(SAMPLE_INVOICE_TEXT);

        assertThat(invoice).isNotNull();
        assertThat(invoice.vendor()).isNotBlank();
        assertThat(invoice.totalAmount()).isNotNull();
        assertThat(invoice.lineItems()).isNotEmpty();
    }

    @Test
    void promptEngineeredJsonExtractsInvoiceFieldsFromGemini() {
        Invoice invoice = structuredOutputService.promptEngineeredJson(
                structuredOutputService.geminiChatClient(), SAMPLE_INVOICE_TEXT);

        assertThat(invoice).isNotNull();
        assertThat(invoice.vendor()).isNotBlank();
    }
}
