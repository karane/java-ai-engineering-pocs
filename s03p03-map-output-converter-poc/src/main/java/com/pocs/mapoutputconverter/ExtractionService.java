package com.pocs.mapoutputconverter;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Service;

/**
 * Both converters are prompt-engineered, not a provider-native JSON mode: they append a
 * format instruction string to the prompt and then parse whatever text comes back, so
 * their reliability rides entirely on how faithfully a model follows that instruction.
 * Claude (Anthropic) tends to return the bare JSON/CSV the format string asks for, so
 * MapOutputConverter/ListOutputConverter.convert() can be called directly on its output.
 * Gemini more often wraps the same answer in a markdown ```json fence or adds a leading
 * sentence, which breaks a strict convert() call - callers routed to Gemini strip fences
 * defensively (see #stripMarkdownFence below) rather than trusting raw output the way the
 * Claude path can. s03p04 covers each provider's *native* JSON mode (forced tool-use /
 * responseSchema), which is the actual fix for this gap rather than a workaround.
 */
@Service
public class ExtractionService {

    private final MapOutputConverter mapOutputConverter = new MapOutputConverter();
    private final ListOutputConverter listOutputConverter =
            new ListOutputConverter(new DefaultConversionService());

    public Map<String, Object> extractKeyValuePairs(ChatClient chatClient, String text) {
        String format = mapOutputConverter.getFormat();
        String prompt = """
                Extract every field you can identify from the text below as key-value pairs.
                Use short, lower_snake_case keys.

                Text:
                %s

                %s
                """.formatted(text, format);

        String raw = chatClient.prompt().user(prompt).call().content();
        return mapOutputConverter.convert(stripMarkdownFence(raw));
    }

    public List<String> extractList(ChatClient chatClient, String topic) {
        String format = listOutputConverter.getFormat();
        String prompt = """
                List exactly 5 %s.

                %s
                """.formatted(topic, format);

        String raw = chatClient.prompt().user(prompt).call().content();
        return listOutputConverter.convert(stripMarkdownFence(raw));
    }

    /**
     * Gemini's JSON-mode replies are more likely to arrive wrapped in a markdown code
     * fence than Claude's - see the class Javadoc. Stripping it defensively keeps both
     * providers usable with the same StructuredOutputConverter call; Claude output
     * already has no fence to strip, so this is a no-op on that path.
     */
    private String stripMarkdownFence(String raw) {
        String trimmed = raw.strip();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```[a-zA-Z]*\\s*", "");
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        return trimmed.strip();
    }
}
