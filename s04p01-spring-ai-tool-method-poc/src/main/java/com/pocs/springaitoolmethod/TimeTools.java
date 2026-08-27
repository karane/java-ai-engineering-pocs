package com.pocs.springaitoolmethod;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

@Component
public class TimeTools {

    private final Clock clock;

    public TimeTools() {
        this(Clock.systemDefaultZone());
    }

    public TimeTools(Clock clock) {
        this.clock = clock;
    }

    /**
     * Builds the "get_current_date_time" tool callback programmatically via
     * {@code FunctionToolCallback.builder(...)}, rather than via an {@code @Tool}-annotated method.
     */
    public ToolCallback currentDateTimeToolCallback() {
        Supplier<String> currentDateTime = () ->
                LocalDateTime.now(clock).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return FunctionToolCallback.builder("get_current_date_time", currentDateTime)
                .description("Get the current local date and time, formatted as yyyy-MM-dd HH:mm:ss. "
                        + "Use this whenever the user asks about today's date or the current time.")
                .inputType(Void.class)
                .build();
    }
}
