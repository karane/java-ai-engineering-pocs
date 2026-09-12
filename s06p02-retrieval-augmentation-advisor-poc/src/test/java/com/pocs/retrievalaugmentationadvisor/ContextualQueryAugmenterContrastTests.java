package com.pocs.retrievalaugmentationadvisor;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;

import static org.assertj.core.api.Assertions.assertThat;

/** Offline, no-Spring-context tests contrasting {@code ContextualQueryAugmenter}'s two {@code allowEmptyContext} modes directly against a query with NO relevant documents. */
class ContextualQueryAugmenterContrastTests {

    private static final Query QUESTION = new Query("What is the boiling point of tungsten?");

    @Test
    void defaultAugmenterRefusesToAnswerWhenNoDocumentsAreFound() {
        QueryAugmenter strict = ContextualQueryAugmenter.builder().build(); // allowEmptyContext defaults to false

        Query augmented = strict.augment(QUESTION, List.of());

        assertThat(augmented.text())
                .contains("outside your knowledge base")
                .doesNotContain("tungsten");
    }

    @Test
    void permissiveAugmenterPassesTheOriginalQueryThroughUnchanged() {
        QueryAugmenter permissive = ContextualQueryAugmenter.builder().allowEmptyContext(true).build();

        Query augmented = permissive.augment(QUESTION, List.of());

        assertThat(augmented.text()).isEqualTo(QUESTION.text());
    }

    @Test
    void bothAugmentersInjectDocumentContextIdenticallyWhenDocumentsAreFound() {
        List<Document> documents = List.of(
                Document.builder().text("Tungsten has the highest boiling point of any element: 5,930 C.").build());

        QueryAugmenter strict = ContextualQueryAugmenter.builder().build();
        QueryAugmenter permissive = ContextualQueryAugmenter.builder().allowEmptyContext(true).build();

        Query strictAugmented = strict.augment(QUESTION, documents);
        Query permissiveAugmented = permissive.augment(QUESTION, documents);

        // allowEmptyContext only changes behavior when there ARE no documents - with documents present,
        // both configurations render the identical context-injecting prompt template.
        assertThat(strictAugmented.text()).isEqualTo(permissiveAugmented.text());
        assertThat(strictAugmented.text()).contains("5,930").contains(QUESTION.text());
    }
}
