# s05p04 — Document Transformer Pipeline

Fourth POC of Level 5. Chains the pieces that turn a raw source file into well-formed, richly-tagged
`Document`s ready for a vector store: reader → splitter → enricher(s) → store.

What's demonstrated:

- `TikaDocumentReader` — parses a plain-text source file (Tika also handles PDF/DOCX/HTML/etc.) into
  `Document`s.
- `TokenTextSplitter.builder().withChunkSize()/withMinChunkSizeChars()/...` — token-aware chunking
  (see the Gotcha below on the builder's actual method names).
- `KeywordMetadataEnricher` / `SummaryMetadataEnricher` — attach LLM-generated keyword and summary
  metadata to each chunk, using a `ChatModel` (Gemini, same setup as `s01p02`).
- `DocumentPipelineService` — a `DocumentReader`/`DocumentTransformer`/`VectorStore`-typed pipeline
  chaining all of the above into one `ingest(DocumentReader)` call, landing enriched chunks in a
  `SimpleVectorStore` (from `s05p03`).

## Requirements

- Java 25, Maven
- A Gemini API key for the live demo/live test (the enrichers need a real `ChatModel` call) — `mvn test`
  is otherwise fully green with zero keys, using a canned-response fake `ChatModel`.

## Setup

```sh
export GEMINI_API_KEY=AQ...
```

No POC-local `.gitignore` needed — the repo-root one already covers `target/`.

## Run the tests

```sh
mvn test
```

If your active SDKMAN Java isn't 25, prefix with `JAVA_HOME`/`PATH` as in the repo's root `CLAUDE.md`.

- No `GEMINI_API_KEY`: context-load test + `DocumentPipelineServiceTests` run (4 tests) — a real
  `TikaDocumentReader` and `TokenTextSplitter` run for real, and the enrichers run against a
  canned-response `FakeChatModel` instead of a live call, so the whole pipeline's wiring is proven with
  zero keys. The live test skips.
- With `GEMINI_API_KEY` set: all 5 run, including a live pipeline run producing real Gemini-generated
  keywords and a summary for the bundled `sample-document.txt`.

## Run the app

```sh
mvn spring-boot:run
```

Console-only. With `GEMINI_API_KEY` set, ingests `src/main/resources/sample-document.txt` through the
full pipeline and prints each chunk's generated keywords/summary; without it, explains and exits cleanly.

## Gotcha: the roadmap's builder method names don't quite match the real API

The roadmap names `TokenTextSplitter.builder().chunkSize()/chunkOverlap()`. The actual 1.1.8 builder is
`TokenTextSplitter.builder().withChunkSize(int).withMinChunkSizeChars(int).withMinChunkLengthToEmbed(int)
.withMaxNumChunks(int).withKeepSeparator(boolean)` — note the `with`-prefixed method names (an older
Spring AI builder convention that predates the `chunkSize()`-style unprefixed builders used elsewhere in
this repo, e.g. `PgVectorStore.builder()...dimensions()`), and there's no `chunkOverlap()` at all — this
splitter is token-count-based via `jtokkit`, not a sliding-window character splitter, so "overlap" isn't
a concept it exposes.

## Key API Reference

| API | Purpose |
|---|---|
| `TikaDocumentReader(Resource)` | Parses PDF/DOCX/plain text/etc. into `Document`s |
| `TokenTextSplitter.builder().withChunkSize(int)...build()` | Token-count-based chunking |
| `KeywordMetadataEnricher.builder(ChatModel).keywordCount(int).build()` | LLM-generated keywords per chunk (`excerpt_keywords` metadata key) |
| `new SummaryMetadataEnricher(ChatModel, List<SummaryType>)` | LLM-generated summary per chunk (`section_summary` metadata key) |
| `DocumentReader.read()` / `DocumentTransformer.apply(List<Document>)` | The two abstractions the whole pipeline is built from |

## What this proves

- A full ingestion pipeline — read, split, enrich (twice), store — composes cleanly through the
  `DocumentReader`/`DocumentTransformer`/`VectorStore` abstractions, with no pipeline-specific glue code
  beyond a simple `for` loop applying each transformer in turn.
- The two LLM-backed enrichers (which need a real `ChatModel` call, unlike the purely mechanical
  reader/splitter) can still be tested offline by substituting a trivial canned-response fake `ChatModel`
  — proving the pipeline's *wiring* is correct without needing every test to hit a live API.
- The live path genuinely calls Gemini and gets back real, sensible keywords and a summary for the
  bundled sample document, confirmed by an actual `mvn test` run with `GEMINI_API_KEY` set.

Next on the roadmap: `s05p05-redis-vector-store-poc` — the stretch POC storing vectors in Redis Stack
instead of Postgres.
