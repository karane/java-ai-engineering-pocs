package com.pocs.toolcontext;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AccountLookupTool {

    /** Stand-in for a per-user account store - keyed by the "userId" this POC injects via ToolContext. */
    private static final Map<String, String> ACCOUNT_TIERS = Map.of(
            "user-42", "Gold",
            "user-7", "Standard"
    );

    private static final Map<String, String> PREFERRED_CURRENCY = Map.of(
            "user-42", "EUR",
            "user-7", "USD"
    );

    @Tool(description = "Look up the requesting user's account tier and preferred currency so pricing "
            + "for a product can be personalized. Always call this before quoting a price.")
    public String getAccountSummary(
            @ToolParam(description = "The product name the user is asking about the price of") String productName,
            ToolContext toolContext) {

        Object userIdValue = toolContext.getContext().get("userId");
        String userId = userIdValue == null ? null : userIdValue.toString();

        if (userId == null || !ACCOUNT_TIERS.containsKey(userId)) {
            return "Unknown user - no account tier or currency preference on file for product '"
                    + productName + "'.";
        }

        String tier = ACCOUNT_TIERS.get(userId);
        String currency = PREFERRED_CURRENCY.get(userId);
        return "User '" + userId + "' is on the '" + tier + "' tier and prefers pricing in " + currency
                + " for product '" + productName + "'.";
    }
}
