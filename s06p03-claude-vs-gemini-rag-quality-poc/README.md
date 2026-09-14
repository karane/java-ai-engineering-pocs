# s06p03 — Claude vs. Gemini RAG Quality

One identical RAG pipeline — a fixed in-memory knowledge base, a deterministic fake embedding model for
retrieval, and a `QuestionAnswerAdvisor` wired onto `ChatClient` — run twice, swapping only the underlying
`ChatModel` bean between Anthropic's `AnthropicChatModel` and Google's `GoogleGenAiChatModel` (Gemini).
Plus a JUnit 5 golden-dataset harness that scores groundedness for both.

## Requirements

- Java 25
- Maven
- An Anthropic API key and a Gemini API key (a Google AI Studio API key — no GCP project/ADC needed) —
  only for the live comparison test and the demo app; everything else runs with zero keys.

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
export GEMINI_API_KEY=...
```

## What's identical vs. what differs

| Piece | Identical across both providers? |
|---|---|
| Knowledge base (`KnowledgeBaseConfig`, 8 fixed documents about a fictional app, "Aurora Notes") | Yes |
| Embedding model for retrieval (`DeterministicFakeEmbeddingModel`) | Yes — never touches either provider |
| `VectorStore` (`SimpleVectorStore`, pre-loaded at startup) | Yes |
| Retrieval advisor (`QuestionAnswerAdvisor`, same `topK`/`similarityThreshold`) | Yes (`ChatClientConfig.ragAdvisor`) |
| `ChatModel` (generation) | **No** — `AnthropicChatModel` vs. `GoogleGenAiChatModel` |

Because retrieval is 100% identical and deterministic, any difference observed in the two providers'
answers is attributable to the generation step alone — which is the whole point of this POC.

### Why a fake embedding model, and why `topK` = the whole knowledge base

`DeterministicFakeEmbeddingModel` hashes text into a fixed-size vector: identical text always produces
the identical vector, and it needs no API key or network call. It is deliberately *not* semantically
meaningful, though — two hash vectors being close doesn't mean the two texts are topically related. With
the knowledge base this small (8 documents), retrieving all of them on every query (`topK=8`,
`similarityThreshold` set to accept-all) sidesteps that limitation entirely, so both providers always see
the same 8-document context and the comparison stays focused on generation quality, not retrieval-ranking
noise from the fake embedding model. A real embedding model (see `s05p01`/`s05p02`/`s05p03`) would let you
shrink `topK` back down; that's out of scope here on purpose.

## How groundedness and citation behavior would be compared

**Groundedness** (this POC's scored dimension): `GroundednessScorer` checks, for each golden-dataset
question, what fraction of a small set of expected facts (short substrings lifted straight from the
knowledge base — e.g. `"Priya Malhotra"`, `"$6"`, `"30 days"`) actually appear in the model's answer. A
fully grounded answer contains every expected fact; a hallucinated or evasive answer scores lower. This is
a simple, zero-extra-API-call proxy — a fuller evaluation would swap it for an LLM-as-judge call (see
`s06p05-rag-evaluation-poc` later on the roadmap) that can also penalize *unsupported* claims the answer
adds beyond the retrieved context, not just missing ones.

**Citation behavior** (observed qualitatively, not scored numerically here): run the demo
(`mvn spring-boot:run` with both keys set) and compare how each provider attributes its answer to the
context. In this POC's own live runs, Claude tends to answer directly and match the source wording closely
("Aurora Notes Pro costs $6 per month"), while Gemini tends toward bullet-point restatement of the same
facts; neither model was instructed to cite explicitly (the `QuestionAnswerAdvisor`'s default prompt
template doesn't ask for citations), so neither produces inline source markers — that would need a custom
prompt template or `RetrievalAugmentationAdvisor` step, which is `s06p02`'s territory. During development,
before `topK` was widened to cover the full 8-document knowledge base, both providers correctly refused to
answer from outside the provided context (e.g. "I can't answer that question based on the provided
context") rather than falling back to pretraining knowledge, whenever the relevant document happened not
to be retrieved — exactly the failure mode groundedness scoring exists to catch, and worth reproducing by
temporarily lowering `RETRIEVE_ALL_DOCUMENTS_TOP_K` in `ChatClientConfig` if you want to see it again.

## What the JUnit harness proves

- **`GoldenDatasetHarnessTest`** (no API key needed, always runs): builds the *exact* same
  `SimpleVectorStore` + `QuestionAnswerAdvisor` pipeline the real provider beans use, but backed by
  `StubEchoChatModel` — a fake `ChatModel` that deterministically echoes the full prompt it receives
  instead of calling any LLM. Since `QuestionAnswerAdvisor` injects retrieved documents straight into that
  prompt, every golden-dataset fact showing up in the echoed "answer" is proof that retrieval, the advisor
  wiring, and `GroundednessScorer` itself all work correctly — independent of any real model's behavior. A
  bug in any of those three would fail this test even though no network call is ever made.
- **`ClaudeVsGeminiRagLiveTest`** (gated on `ANTHROPIC_API_KEY` / `GEMINI_API_KEY`, one
  `@EnabledIfEnvironmentVariable`-gated `@ParameterizedTest` method per provider, same pattern as
  `s04p03`'s live tests): runs the full golden dataset through both *real* RAG pipelines and asserts each
  answer's groundedness score is at least 0.5 (a real model paraphrases, so exact substring matches aren't
  guaranteed the way they are against the stub — unlike the harness test above, which requires 100%).
  Skips gracefully — not a failure — when a key isn't set.

## Run the tests

```sh
mvn test
```

- No keys set: `contextLoads()` and `GoldenDatasetHarnessTest` run (2 tests), `ClaudeVsGeminiRagLiveTest`
  skips entirely.
- Both keys exported: all tests run, including 16 parameterized live-comparison cases (8 golden questions
  × 2 providers) against the real APIs.

## Run the app

```sh
mvn spring-boot:run
```

Runs one fixed question through both RAG pipelines side by side, printing each provider's answer, for
whichever provider(s) have a key set.

## Deviations from the roadmap wording

Same deviation as `s01p02`/`s04p03`: the roadmap names `VertexAiGeminiChatModel`, but Spring AI BOM 1.1.8
has no such artifact — Gemini support in this BOM line ships as `spring-ai-starter-model-google-genai` /
`GoogleGenAiChatModel` / `GoogleGenAiChatOptions` (API-key auth by default, not Vertex AI / ADC). The
comparison and everything else in the roadmap line is otherwise implemented as described.
