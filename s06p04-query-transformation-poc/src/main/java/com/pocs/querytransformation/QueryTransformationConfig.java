package com.pocs.querytransformation;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** Wires an in-memory {@link VectorStore} and a fake {@link ChatModel} for this POC. */
@Configuration
public class QueryTransformationConfig {

    @Bean
    public VectorStore vectorStore() {
        return SimpleVectorStore.builder(new DeterministicFakeEmbeddingModel()).build();
    }

    @Bean
    @Primary
    public ChatModel fakeChatModel() {
        return new FakeTransformationChatModel();
    }
}
