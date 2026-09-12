# s06p02 — RetrievalAugmentationAdvisor

Second POC of Level 6 (RAG). `s06p01` proved `QuestionAnswerAdvisor` — a simple, one-shot
retrieval-and-inject advisor. This POC proves `RetrievalAugmentationAdvisor`: Spring AI's full
modular RAG pipeline advisor, built from composable pieces (`DocumentRetriever`, `DocumentJoiner`,
`QueryAugmenter`) that can be swapped independently.

What's demonstrated:

- `RetrievalAugmentationAdvisor.builder().documentRetriever(...).build()` wired into
  `ChatClient.prompt().advisors(...)`.
- `VectorStoreDocumentRetriever.builder().vectorStore(...).similarityThreshold().topK().build()` —
  the retriever that pulls semantically similar documents from a `VectorStore`.
- `ContextualQueryAugmenter` — the advisor's default query-rewriting step. Contrasted in two
  configurations: the default (`allowEmptyContext=false`, declines to answer out-of-scope
  questions) vs. an explicit `allowEmptyContext(true)` (falls back to the model's own knowledge).
- `DocumentJoiner` (via `ConcatenationDocumentJoiner`) merging and deduplicating documents
  retrieved from **two different** `SimpleVectorStore`s, wired through a custom
  `MultiSourceDocumentRetriever` into a single `RetrievalAugmentationAdvisor` pipeline.

## Requirements

- Java 25, Maven
- A Gemini API key for real embeddings and an Anthropic API key for the real chat model (both
  optional — `mvn test` is fully green without either, using a deterministic fake `EmbeddingModel`
  and a deterministic echo `ChatModel`)

## Setup

```sh
export GEMINI_API_KEY=AQ...
export ANTHROPIC_API_KEY=sk-ant-...
```

No POC-local `.gitignore` needed — the repo-root one already covers `target/`.

## Run the tests

```sh
mvn test
```

If your active SDKMAN Java isn't 25, prefix with `JAVA_HOME`/`PATH` as in the repo's root `CLAUDE.md`.

- No API keys: context-load test + all offline test classes run (10 tests) — `MultiSourceDocumentRetrieverTests`
  and `RetrievalAugmentationPipelineTests` use a small deterministic hash-based fake `EmbeddingModel`
  and a `StubEchoChatModel` that echoes the prompt it receives back as its answer, so the full
  `RetrievalAugmentationAdvisor` pipeline (retrieval, joining, query augmentation) is proven end to
  end with zero network calls. `ContextualQueryAugmenterContrastTests` exercises the augmenter
  directly with no `VectorStore`/`ChatModel` at all. The live test skips.
- With both `GEMINI_API_KEY` and `ANTHROPIC_API_KEY` set: all 12 run, including a live grounded
  answer using real `gemini-embedding-001` embeddings and a real `claude-haiku-4-5` response.

## Run the app

```sh
mvn spring-boot:run
```

Console-only. With both API keys set: loads two small knowledge bases, asks an in-scope question via
the strict advisor, asks an out-of-scope question via both the strict and permissive advisors side
by side, and asks a question groundable only by merging both vector stores via `DocumentJoiner`.
Without both keys, it explains and exits cleanly.

## Key API Reference

| API | Purpose |
|---|---|
| `RetrievalAugmentationAdvisor.builder().documentRetriever(...).build()` | Full modular RAG pipeline as a `ChatClient` advisor |
| `VectorStoreDocumentRetriever.builder().vectorStore().similarityThreshold().topK().build()` | Semantic-similarity document retrieval |
| `ContextualQueryAugmenter.builder().allowEmptyContext(Boolean).build()` | Query rewriting; controls behavior when no documents are found |
| `DocumentJoiner` / `ConcatenationDocumentJoiner` | Merges and deduplicates documents from multiple queries/sources |
| `RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT` | Context key the advisor stores retrieved documents under, on the response metadata |

## What this proves

- `RetrievalAugmentationAdvisor` really does run retrieval → joining → query augmentation before
  the prompt ever reaches the `ChatModel` — proven offline by echoing the augmented prompt straight
  back out of a stub `ChatModel`.
- `ContextualQueryAugmenter`'s `allowEmptyContext` flag is the single knob that decides whether an
  out-of-scope question gets politely declined (`allowEmptyContext=false`, the advisor's default) or
  quietly forwarded to the model to answer from its own training data (`allowEmptyContext=true`) —
  same retriever, same "no documents found" outcome, two very different final prompts.
- `DocumentJoiner` genuinely merges and deduplicates: a document present in both vector stores
  (same id) appears exactly once in the joined result, and an answer that needs facts from *both*
  stores is only fully grounded once `MultiSourceDocumentRetriever` combines them — neither store
  alone is enough.

Next on the roadmap: `s06p03-claude-vs-gemini-rag-quality-poc` — the identical RAG pipeline with
only the `ChatModel` bean swapped between Anthropic and Gemini, comparing groundedness.
