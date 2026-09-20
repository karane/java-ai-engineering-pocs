package com.pocs.contextualcompression;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

/** A small, deliberately mixed-topic corpus, split into chunks with {@link TokenTextSplitter} - the same chunking building block as {@code s05p04}. */
public final class SampleCorpus {

    private SampleCorpus() {
    }

    public static List<Document> chunkedDocuments() {
        List<Document> rawDocuments = List.of(
                Document.builder().text("""
                        Spring AI is a Java framework for building applications powered by large language
                        models. It provides a portable Spring-friendly API across many model providers,
                        including Anthropic, OpenAI, Google Vertex AI, and Amazon Bedrock, so an application
                        can switch chat or embedding providers with a configuration change rather than a
                        code rewrite. Spring AI also standardizes cross-cutting RAG concerns such as
                        document readers, text splitters, vector stores, and retrieval advisors.
                        """).metadata("topic", "spring-ai").build(),
                Document.builder().text("""
                        Retrieval-augmented generation, or RAG, grounds a language model's answers in an
                        external knowledge base instead of relying only on what the model memorized during
                        training. A typical RAG pipeline embeds a user's query, retrieves the most similar
                        chunks from a vector store, and injects those chunks into the prompt as context
                        before asking the model to answer. Reranking and filtering steps can sit between
                        retrieval and generation to keep only the most relevant chunks in that context.
                        """).metadata("topic", "spring-ai").build(),
                Document.builder().text("""
                        The Roman Republic was a form of government in ancient Rome that lasted roughly
                        from 509 BC to 27 BC, when it gave way to the Roman Empire under Augustus. Power
                        was distributed among elected magistrates, the Senate, and various popular
                        assemblies, in a system of checks meant to prevent any single person from
                        accumulating permanent power - a balance that famously broke down in the civil
                        wars of the first century BC.
                        """).metadata("topic", "roman-history").build(),
                Document.builder().text("""
                        Julius Caesar's crossing of the Rubicon river in 49 BC was a deliberate act of
                        civil war: Roman law forbade a general from bringing his army into Italy proper,
                        and by doing so anyway Caesar made armed conflict with the Senate and Pompey
                        unavoidable. The phrase "crossing the Rubicon" has since come to mean passing a
                        point of no return.
                        """).metadata("topic", "roman-history").build(),
                Document.builder().text("""
                        Pour-over coffee brewing relies on gravity to pull hot water through a bed of
                        ground coffee and a paper or metal filter. Water temperature, grind size, and pour
                        rate all affect extraction: water that's too hot or a grind that's too fine can
                        over-extract and taste bitter, while water that's too cool or a grind that's too
                        coarse can under-extract and taste sour or weak.
                        """).metadata("topic", "coffee").build(),
                Document.builder().text("""
                        Espresso differs from pour-over brewing mainly in pressure: a espresso machine
                        forces hot water through very finely ground, tightly packed coffee at roughly nine
                        bars of pressure, producing a concentrated shot with a layer of crema on top. That
                        pressure is also why espresso extracts so much faster than gravity-fed methods -
                        typically twenty-five to thirty seconds, versus several minutes for a pour-over.
                        """).metadata("topic", "coffee").build());

        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(60)
                .withMinChunkSizeChars(20)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(50)
                .build();

        return splitter.apply(rawDocuments);
    }
}
