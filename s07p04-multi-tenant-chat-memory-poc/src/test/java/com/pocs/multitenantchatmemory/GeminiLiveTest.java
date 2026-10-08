package com.pocs.multitenantchatmemory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.ai.chat.memory.ChatMemory;

@SpringBootTest
@Testcontainers
@ActiveProfiles("gemini")
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class GeminiLiveTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    @Qualifier("tenantAChatClient")
    private ChatClient tenantAChatClient;

    @Autowired
    @Qualifier("tenantBChatClient")
    private ChatClient tenantBChatClient;

    @Test
    void multiTenantMemoryIsolationIsMaintained() {
        String conversationId = "live-test-gemini";

        tenantAChatClient.prompt().user("I am Tenant A. Remember my favorite food is pizza.")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call().content();

        tenantBChatClient.prompt().user("I am Tenant B. Remember my favorite food is sushi.")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call().content();

        String responseA = tenantAChatClient.prompt().user("What is my favorite food?")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call().content();
        
        String responseB = tenantBChatClient.prompt().user("What is my favorite food?")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call().content();

        assertThat(responseA.toLowerCase()).contains("pizza").doesNotContain("sushi");
        assertThat(responseB.toLowerCase()).contains("sushi").doesNotContain("pizza");
    }
}
