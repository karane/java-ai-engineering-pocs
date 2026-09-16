# s06p04 — Query Transformation

Fourth POC of Level 6. Demonstrates Spring AI's pre-retrieval query transformers — the pieces of a
RAG pipeline that rewrite the user's query *before* it ever reaches a vector store:

- `CompressionQueryTransformer.builder().chatClientBuilder(...).build()` — condenses a multi-turn
  conversation history plus a follow-up question ("How tall is it?") into one standalone query
  ("How tall is the Eiffel Tower?").
- `RewriteQueryTransformer.builder().chatClientBuilder(...).build()` — rewrites a verbose,
  conversational query into one optimized for vector search.
- `MultiQueryExpander.builder().chatClientBuilder(...).numberOfQueries(n).build()` — fans a single
  query out into `n` variant phrasings, to widen retrieval recall.
- A multi-query fan-out retrieval flow: expand → retrieve per variant via
  `VectorStoreDocumentRetriever` → merge and deduplicate by document id.

## Why a fake `ChatModel`

All three transformers call an LLM to do the actual rewriting. To keep `mvn test` green with zero
API keys, this POC supplies a deterministic test double, `FakeTransformationChatModel`, as the
`@Primary` `ChatModel` bean. It inspects the *rendered* prompt text for the marker phrases unique
to each transformer's default `PromptTemplate` (`"standalone query"` + `"Conversation history:"`
for compression, `"Rewritten query:"` for rewriting, `"Query variants:"` for expansion) and returns
a canned-but-content-derived response — enough to prove the wiring, prompt rendering, response
parsing, and merge/dedup logic all work, without ever calling a real model.

`spring-ai-starter-model-anthropic` is still on the classpath and auto-configures a real
`AnthropicChatModel` bean; marking the fake `ChatModel` `@Primary` is what makes Spring AI's
autoconfigured `ChatClient.Builder` bean resolve to the fake one by default. The live tests
sidestep this by autowiring `AnthropicChatModel` directly (a concrete type, so there's no
ambiguity) and building their own real `ChatClient.Builder` from it.

## Requirements

- Java 25, Maven
- An Anthropic API key for the live tests and live demo path (optional — `mvn test` is fully green
  without it)

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
```

No POC-local `.gitignore` needed — the repo-root one already covers `target/`.

## Run the tests

```sh
mvn test
```

If your active SDKMAN Java isn't 25, prefix with `JAVA_HOME`/`PATH` as in the repo's root
`CLAUDE.md`.

- No `ANTHROPIC_API_KEY`: context-load test + all 4 `QueryTransformationServiceTests` run against
  `FakeTransformationChatModel` (5 tests) — fully offline, proving each transformer's wiring and
  the multi-query dedup logic. `QueryTransformationLiveTest`'s 4 tests skip.
- With `ANTHROPIC_API_KEY` set: all 9 run, including the 4 live tests exercising the same three
  transformers against a real Claude model.

## Run the app

```sh
mvn spring-boot:run
```

Console-only. Populates a `SimpleVectorStore` with a few sentences about Paris and Japan, then runs
each transformer and the deduplicated multi-query retrieval flow — using the fake `ChatModel` by
default (works with zero keys), or a real Claude model if `ANTHROPIC_API_KEY` is set.

## Key API Reference

| API | Purpose |
|---|---|
| `CompressionQueryTransformer.builder().chatClientBuilder(...)` | Conversation history + follow-up → standalone query |
| `RewriteQueryTransformer.builder().chatClientBuilder(...)` | Verbose query → retrieval-optimized query |
| `MultiQueryExpander.builder().chatClientBuilder(...).numberOfQueries(n)` | One query → `n` variant phrasings |
| `VectorStoreDocumentRetriever.builder().vectorStore(...).topK(...)` | Retrieve documents for a single `Query` |
| `Query.builder().text().history().build()` / `new Query(text)` | The transformers' shared input/output type |

## What this proves

- All three query transformers are pure `ChatClient`-backed components — they can be tested fully
  offline by swapping in any `ChatModel`, fake or real, without changing a line of the transformer
  wiring itself.
- `MultiQueryExpander`'s variant count is a best-effort request, not a guarantee: if the model's
  response doesn't parse into exactly the requested number of newline-separated lines, Spring AI
  falls back to returning the original query unchanged — worth knowing before asserting an exact
  count against a real model in your own tests.
- Fanning a query out into variants and retrieving separately for each one produces overlapping
  results across variants; merging by document id (rather than by list-concatenation) is what
  keeps the final result set free of duplicates.

Next on the roadmap: `s06p05-rag-evaluation-poc` — `RelevancyEvaluator` and `FactCheckingEvaluator`
built into Spring AI, LLM-as-judge scoring using a second `ChatClient`, and JUnit 5 regression
tests asserting evaluator score thresholds per provider.
