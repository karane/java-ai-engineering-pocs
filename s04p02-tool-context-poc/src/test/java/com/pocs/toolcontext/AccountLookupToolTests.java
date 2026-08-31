package com.pocs.toolcontext;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AccountLookupToolTests {

    private final AccountLookupTool accountLookupTool = new AccountLookupTool();

    @Test
    void toolParamDescriptionFlowsIntoGeneratedSchema() {
        ToolCallback[] callbacks = ToolCallbacks.from(accountLookupTool);
        assertThat(callbacks).hasSize(1);

        ToolDefinition definition = callbacks[0].getToolDefinition();
        assertThat(definition.name()).isEqualTo("getAccountSummary");
        assertThat(definition.description()).contains("personalized");
        // @ToolParam(description = ...) on productName must show up as that property's schema description.
        assertThat(definition.inputSchema()).contains("The product name the user is asking about");
        // ToolContext is a framework-injected parameter - it must never leak into the JSON schema Claude sees.
        assertThat(definition.inputSchema()).doesNotContain("toolContext");
    }

    @Test
    void readsUserIdInjectedViaToolContext() {
        ToolContext toolContext = new ToolContext(Map.of("userId", "user-42"));

        String summary = accountLookupTool.getAccountSummary("Orbital Widget", toolContext);

        assertThat(summary).contains("user-42").contains("Gold").contains("EUR").contains("Orbital Widget");
    }

    @Test
    void gracefullyHandlesUnknownUserId() {
        ToolContext toolContext = new ToolContext(Map.of("userId", "unknown-user"));

        String summary = accountLookupTool.getAccountSummary("Orbital Widget", toolContext);

        assertThat(summary).contains("Unknown user");
    }

    @Test
    void gracefullyHandlesMissingToolContextEntry() {
        ToolContext toolContext = new ToolContext(Map.of());

        String summary = accountLookupTool.getAccountSummary("Orbital Widget", toolContext);

        assertThat(summary).contains("Unknown user");
    }
}
