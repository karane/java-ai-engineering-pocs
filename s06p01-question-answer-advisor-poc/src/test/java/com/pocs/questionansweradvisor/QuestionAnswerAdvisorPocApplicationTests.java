package com.pocs.questionansweradvisor;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QuestionAnswerAdvisorPocApplicationTests {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private QuestionAnsweringService questionAnsweringService;

    @Test
    void contextLoadsAndWiresVectorStoreChatClientAndService() {
        assertThat(vectorStore).isNotNull();
        assertThat(chatClient).isNotNull();
        assertThat(questionAnsweringService).isNotNull();
    }
}
