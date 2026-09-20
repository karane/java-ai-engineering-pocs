# s06p06 — Contextual Compression RAG

Stretch POC of Level 6. A naive RAG pipeline stuffs every retrieved chunk into the prompt. This
POC adds a "contextual compression" stage between retrieval and generation: over-retrieve a wide
candidate set, rerank it with a cross-encoder-style scoring call, drop the low-relevance chunks,
then trim what's left to fit a provider's token budget.

What's demonstrated:

- **Over-retrieval + reranking** — `VectorStore.similaritySearch` pulls back `topK=10` candidates
  by embedding similarity alone; `RerankingService` then scores each (query, passage) pair
  directly via a single `ChatClient` call per candidate, the way a real cross-encoder model would
  (as opposed to a bi-encoder, which is all `similaritySearch` itself does).
- **`DocumentPostProcessor`** — `RelevanceThresholdPostProcessor` implements Spring AI's
  `org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor`
  (`List<Document> process(Query, List<Document>)`), reranking and dropping anything below a
  relevance threshold, and is wired as a `documentPostProcessors` stage of a real
  `RetrievalAugmentationAdvisor`.
- **Token budgeting** — `TokenBudget` estimates tokens with the classic chars/4 heuristic and
  trims a ranked chunk list to fit a per-provider budget, proving a Claude-class budget admits
  fewer chunks than a (proportionally larger) Gemini-class budget for the same reranked set.

## Requirements

- Java 25, Maven
- `ANTHROPIC_API_KEY` + `GEMINI_API_KEY` for the live paths (optional — `mvn test` is fully green
  without them, using deterministic fakes for both the embedding model and the scoring chat model)

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
export GEMINI_API_KEY=AQ...
```

No POC-local `.gitignore` needed — the repo-root one already covers `target/`.

## Run the tests

```sh
mvn test
```

If your active SDKMAN Java isn't 25, prefix with `JAVA_HOME`/`PATH` as in the repo's root
`CLAUDE.md`.

- No API keys: 13 tests run — `RerankingServiceTests`, `RelevanceThresholdPostProcessorTests`,
  `TokenBudgetTests`, `ContextualCompressionServiceTests`, and
  `ContextualCompressionRagPocApplicationTests` (Spring context load) — all fully offline via
  `DeterministicFakeEmbeddingModel` (retrieval) and `DeterministicScoringChatModel` (a word-overlap
  stand-in for the cross-encoder scoring call). The live test skips.
- With both keys set: 14 tests run, including a live rerank against a real Claude call that
  proves an off-topic candidate (Roman history) gets dropped from a RAG query about
  retrieval-augmented generation.

## Run the app

```sh
mvn spring-boot:run
```

Console-only. With both keys set, ingests a small mixed-topic corpus (Spring AI/RAG, Roman
history, coffee brewing), over-retrieves candidates for a RAG question, reranks + filters them
with real Claude scoring calls, and prints how many chunks fit under the Claude-class vs.
Gemini-class demo budgets. Without both keys, explains and exits cleanly.

## Design note: demo-scaled token budgets

Real context windows (200K tokens for `claude-haiku-4-5-20251001`, 1M for
`gemini-3.5-flash-lite`) are far bigger than a handful of demo chunks could ever exceed, and the
right number to budget *retrieved context* against isn't the raw window anyway — it also has to
leave room for the system prompt, conversation history, and the model's own output. `TokenBudget`
keeps both real window sizes as reference constants, but demonstrates trimming against a much
smaller "context budget" pair that preserves the real 1:5 window ratio between the two providers —
small enough that a few chunks can actually exceed the tighter one. See the class Javadoc for the
full reasoning.

## Key API Reference

| API | Purpose |
|---|---|
| `VectorStore.similaritySearch(SearchRequest.builder().topK(10)...)` | Over-retrieve a wide candidate set |
| `ChatClient.prompt().user(...).call().content()` | The cross-encoder-style scoring call, one per candidate |
| `org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor` | `process(Query, List<Document>)` — the low-relevance filter |
| `RetrievalAugmentationAdvisor.builder().documentPostProcessors(...)` | Wires the filter into a real RAG advisor pipeline |
| `VectorStoreDocumentRetriever.builder().vectorStore(...).topK(...)` | The advisor's own retrieval stage |

## What this proves

- A single-call-per-candidate `ChatClient` scoring loop is a workable, provider-agnostic stand-in
  for a purpose-trained cross-encoder reranker, and it measurably reorders and filters an
  over-retrieved candidate set.
- Spring AI's `DocumentPostProcessor` contract is simple enough to implement directly (one method,
  `Query` in, filtered `List<Document>` out) and drops straight into `RetrievalAugmentationAdvisor`
  as a real pipeline stage, not just a standalone utility.
- Token budgeting is what turns "how big is the model's context window" into "how many of my
  reranked chunks actually fit" — the same reranked, filtered list yields fewer included chunks
  under a smaller budget than a larger one, which is the whole point of measuring the budget
  before injecting context, not after.

Next on the roadmap (Level 7): chat memory and conversational state.
