package com.pocs.multiservermcpaggregation;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/** Server B's text-utility tools, active under the "server-b" profile. */
@Component
public class TextTools {

    @Tool(description = "Convert a string to upper case")
    public String shout(@ToolParam(description = "The text to upper-case") String text) {
        return text.toUpperCase();
    }

    @Tool(description = "Count the words in a string, splitting on whitespace")
    public int wordCount(@ToolParam(description = "The text to count words in") String text) {
        String trimmed = text.trim();
        return trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
    }

    @Tool(description = "[server-b] Echo a diagnostic ping, identifying which server answered")
    public String ping() {
        return "pong from server-b (text-tools-server)";
    }
}
