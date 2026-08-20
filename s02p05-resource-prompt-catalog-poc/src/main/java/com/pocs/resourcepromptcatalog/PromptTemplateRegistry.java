package com.pocs.resourcepromptcatalog;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

/**
 * Loads and caches {@link PromptTemplate} instances from classpath .st resource
 * files under {@code prompts/}, keyed by a short logical name such as
 * "summarize:v1" or "classify". This is the "externalized prompt library"
 * abstraction the POC demonstrates: prompt text lives in version-controlled
 * resource files instead of being hard-coded into Java strings, and callers
 * look templates up by key rather than by file path.
 *
 * <p>Deliberately provider-agnostic: it knows nothing about ChatClient or any
 * specific model - it only produces rendered prompt strings.
 */
@Service
public class PromptTemplateRegistry {

    private static final String PROMPTS_LOCATION = "classpath:prompts/";

    /** key -> classpath filename, the catalog this registry serves. */
    private static final Map<String, String> CATALOG = Map.of(
            "summarize:v1", "summarize-v1.st",
            "summarize:v2", "summarize-v2.st",
            "classify", "classify.st",
            "translate", "translate.st"
    );

    private final Map<String, PromptTemplate> cache = new ConcurrentHashMap<>();

    /**
     * Returns the (cached, lazily loaded) PromptTemplate registered under the
     * given key.
     *
     * @throws IllegalArgumentException if no template is registered under that key
     */
    public PromptTemplate get(String key) {
        return cache.computeIfAbsent(key, this::load);
    }

    /** Renders the template registered under {@code key} with the given variables. */
    public String render(String key, Map<String, Object> variables) {
        return get(key).render(variables);
    }

    /** All keys currently registered in the catalog. */
    public Iterable<String> keys() {
        return CATALOG.keySet();
    }

    /**
     * Renders both versions of a prompt (e.g. "summarize:v1" and "summarize:v2")
     * against the same input variables, so the two renderings can be compared
     * side by side - the A/B half of the catalog. The caller decides what to do
     * with the two rendered prompts (e.g. send both to a ChatClient and compare
     * the responses).
     */
    public Map<String, String> renderVersionsForComparison(String baseKey, Map<String, Object> variables) {
        Map<String, String> rendered = new LinkedHashMap<>();
        for (String key : CATALOG.keySet()) {
            if (key.equals(baseKey) || key.startsWith(baseKey + ":")) {
                rendered.put(key, render(key, variables));
            }
        }
        if (rendered.isEmpty()) {
            throw new IllegalArgumentException("No versioned templates found for base key: " + baseKey);
        }
        return rendered;
    }

    private PromptTemplate load(String key) {
        String filename = CATALOG.get(key);
        if (filename == null) {
            throw new IllegalArgumentException("No prompt template registered under key: " + key);
        }
        Resource resource = new PathMatchingResourcePatternResolver().getResource(PROMPTS_LOCATION + filename);
        return PromptTemplate.builder().resource(resource).build();
    }
}
