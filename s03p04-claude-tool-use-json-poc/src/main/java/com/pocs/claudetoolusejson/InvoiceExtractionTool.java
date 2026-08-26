package com.pocs.claudetoolusejson;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvoiceExtractionTool {

    @Tool(name = "record_invoice", description = "Record the structured fields of an invoice extracted "
            + "from a natural-language description. You MUST call this tool with the extracted fields - "
            + "do not answer in plain text.")
    public String recordInvoice(
            @ToolParam(description = "The vendor or company name that issued the invoice") String vendor,
            @ToolParam(description = "The total amount due, in US dollars, as a decimal number") Double totalAmount,
            @ToolParam(description = "The due date in ISO-8601 format (yyyy-MM-dd)") String dueDate,
            @ToolParam(description = "Short line-item descriptions billed on this invoice") List<String> lineItems) {
        return "Invoice recorded.";
    }
}
