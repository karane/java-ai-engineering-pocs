package com.pocs.prompttemplate;

import java.util.Map;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class PromptTemplateService {

    private final PromptTemplate greetingResourceTemplate;

    public PromptTemplateService(ResourceLoader resourceLoader) {
        Resource resource = resourceLoader.getResource("classpath:templates/greeting.st");
        this.greetingResourceTemplate = PromptTemplate.builder().resource(resource).build();
    }

    public String renderInlineTemplate(String template, Map<String, Object> variables) {
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(template)
                .build();
        return promptTemplate.render(variables);
    }

    /**
     * Renders the "greeting" template, which was loaded once at construction time
     * from src/main/resources/templates/greeting.st via PromptTemplate(Resource) -
     * demonstrating that template text can live in a version-controlled resource
     * file rather than a Java string literal.
     */
    public String renderGreetingFromResource(Map<String, Object> variables) {
        return greetingResourceTemplate.render(variables);
    }
}
