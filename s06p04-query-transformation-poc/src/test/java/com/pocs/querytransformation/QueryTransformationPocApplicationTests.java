package com.pocs.querytransformation;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QueryTransformationPocApplicationTests {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private QueryTransformationService queryTransformationService;

    @Test
    void contextLoadsAndWiresTheFakeChatModelAsPrimaryByDefault() {
        assertThat(vectorStore).isNotNull();
        assertThat(chatClientBuilder).isNotNull();
        assertThat(queryTransformationService).isNotNull();
        // No ANTHROPIC_API_KEY in this environment, so the @Primary fake ChatModel is what the
        // autoconfigured ChatClient.Builder (and this @Autowired-by-type ChatModel) resolve to.
        assertThat(chatModel).isInstanceOf(FakeTransformationChatModel.class);
    }
}
