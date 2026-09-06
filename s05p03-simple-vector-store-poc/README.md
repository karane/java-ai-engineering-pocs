# s05p03 — SimpleVectorStore

Third POC of Level 5. `SimpleVectorStore` is Spring AI's in-memory `VectorStore` implementation — no
database, no external service, JSON-file persistence — ideal for prototyping, small demos, or tests.

What's demonstrated:

- `SimpleVectorStore.builder(EmbeddingModel).build()` — in-memory store construction.
- `VectorStore.add(List<Document>)` / `.similaritySearch(SearchRequest)` — the same abstraction used in
  `s05p02`, this time with zero external dependencies.
- `Document.builder().text().metadata()` — building documents with attached metadata.
- `.save(File)` / `.load(File)` — JSON persistence and reload round-trip.
- `FilterExpressionBuilder` — building a metadata filter expression (`genre == "fantasy"`) and passing
  it into a `SearchRequest` to restrict similarity search results.

## Requirements

- Java 25, Maven
- A Gemini API key for the live demo path (optional — `mvn test` is fully green without it, since
  `SimpleVectorStore` itself has zero external dependencies)

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

- No `GEMINI_API_KEY`: context-load test + all `SimpleVectorStoreServiceTests` run (4 tests) — these use
  a small deterministic hash-based fake `EmbeddingModel` (`DeterministicFakeEmbeddingModel`), so
  `SimpleVectorStore`'s own mechanics (add/search/filter/save/load) are proven with zero keys. The live
  test skips.
- With `GEMINI_API_KEY` set: all 5 run, including a live similarity search using real
  `gemini-embedding-001` vectors.

## Run the app

```sh
mvn spring-boot:run
```

Console-only. With `GEMINI_API_KEY` set, embeds a few book descriptions, runs an unfiltered and a
metadata-filtered similarity search, and saves the store to a temp JSON file; without it, explains and
exits cleanly.

## Gotcha: a naive fake embedding model can produce near-opposite vectors

The fake `EmbeddingModel` used in tests originally built each vector component as
`(text.hashCode() * 31 + i) % 1000`. `String.hashCode()` can be negative, and Java's `%` preserves the
sign of its left operand — so two different strings could end up with vectors pointing in *nearly
opposite* directions (cosine similarity ≈ -1) purely by hash-code coincidence, well outside
`SearchRequest`'s valid `[0,1]` similarity-threshold range. `similaritySearch()` doesn't throw in that
case; it just quietly returns zero results, because every candidate falls below the default threshold.
The fix: use `Math.floorMod` instead of `%` so every vector component stays non-negative, keeping fake
embeddings in a sane, comparable range. Worth knowing if you ever reach for a quick hash-based embedding
stub yourself.

## Key API Reference

| API | Purpose |
|---|---|
| `SimpleVectorStore.builder(EmbeddingModel)` | In-memory store construction |
| `VectorStore.add(List<Document>)` | Embed and store documents |
| `VectorStore.similaritySearch(SearchRequest)` | Query by similarity, optionally filtered |
| `SimpleVectorStore.save(File)` / `.load(File)` | JSON persistence round-trip |
| `FilterExpressionBuilder().eq(key, value).build()` | Build a metadata filter `Filter.Expression` |
| `SearchRequest.builder().filterExpression(Filter.Expression)` | Attach the filter to a search |

## What this proves

- `SimpleVectorStore` is a fully self-contained `VectorStore` — the whole add/search/filter/persist
  cycle is provable with zero external dependencies and zero API keys, using a deterministic fake
  embedding model.
- Metadata filter expressions built via `FilterExpressionBuilder` actually restrict which documents a
  similarity search can return, not just which ones rank highest.
- `.save(File)`/`.load(File)` round-trips the full store (vectors, text, and metadata) faithfully — a
  fresh, empty `SimpleVectorStore` loaded from a previously-saved file behaves identically to the
  original.

Next on the roadmap: `s05p04-document-transformer-poc` — chunking and enriching documents with
`TokenTextSplitter`, `TikaDocumentReader`, and metadata enrichers before they ever reach a vector store.
