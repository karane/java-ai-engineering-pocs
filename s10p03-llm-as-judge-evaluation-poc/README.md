# s10p03 — LLM-as-Judge Evaluation

Spring AI's built-in `RelevancyEvaluator`/`FactCheckingEvaluator` (LLM-as-judge scoring), a custom
non-LLM `Evaluator` implementation, JUnit 5 parameterized golden-dataset tests, and a CI-gated aggregate
quality threshold run against both a real Claude judge and a real Gemini judge.

## What's demonstrated

- **`KnowledgeBaseConfig`** / **`DeterministicFakeEmbeddingModel`** — a tiny fixed knowledge base in a
  `SimpleVectorStore`, retrieved via a hashing-trick embedding model, so the RAG pipeline under evaluation
  runs with zero external services.
- **`RagAnswerService`** / **`RagAnswer`** — the small RAG pipeline being evaluated: `ChatClient` +
  `QuestionAnswerAdvisor`, producing an answer plus the documents retrieved for it.
- **`CannedChatModel`** — a fake `ChatModel` returning fixed text; used as the deterministic "answerer"
  in every test (even live ones) and as the fake "judge" in offline tests.
- **`LlmJudgeEvaluationService`** — wraps Spring AI's `RelevancyEvaluator` and `FactCheckingEvaluator`,
  both built from a "judge" `ChatClient.Builder` — a second chat client, separate from the one that
  produced the answer being judged.
- **`ConcisenessEvaluator`** — a **custom `Evaluator` implementation** that scores word count against a
  budget with zero LLM calls, proving `Evaluator` is a general extension point, not an LLM-only concept.
- **`GoldenDataset`** — the fixed question/grounded-answer pairs scored by every test class.
- **`QualityGate`** — turns a batch of `EvaluationResponse`s into one aggregate pass rate and a
  pass/fail verdict against `DEFAULT_PASS_RATE_THRESHOLD` (0.80) — the CI-gate mechanism.
- **`EvaluationDemoRunner`** — runs the golden dataset through both judges (whichever has a key) and
  prints each gate's verdict.

## Requirements

- Java 25
- Maven
- An Anthropic API key and/or a Gemini API key — only for the live judge tests and the demo app;
  everything else (offline tests, custom evaluator, quality gate) runs with zero keys.

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
export GEMINI_API_KEY=...
```

## Java version

```sh
JAVA_HOME=~/.sdkman/candidates/java/25.0.3-tem \
PATH=~/.sdkman/candidates/java/25.0.3-tem/bin:$PATH \
mvn test
```

## `.gitignore`

No POC-local `.gitignore` needed — the repo root's already excludes `target/` and friends.

## Run the tests

```sh
JAVA_HOME=~/.sdkman/candidates/java/25.0.3-tem PATH=~/.sdkman/candidates/java/25.0.3-tem/bin:$PATH mvn test
```

- No keys set: `contextLoads()`, `ConcisenessEvaluatorTests` (4), `QualityGateTests` (5), and
  `GoldenDatasetOfflineTests` (10 parameterized/plain, backed by `CannedChatModel` as the judge) all run —
  20 tests green, no network. `AnthropicJudgeLiveTest`/`GeminiJudgeLiveTest` skip entirely (2 each).
- Both keys exported: the live tests also run, asserting a real Claude judge and a real Gemini judge each
  clear `QualityGate`'s 0.80 pass-rate threshold on the golden dataset's grounded answers, and that both
  correctly fail an ungrounded answer.

**Status in this environment:** `mvn test` with no API keys is green (24 tests, 4 skipped). Live-path
verification with real `ANTHROPIC_API_KEY`/`GEMINI_API_KEY` and `mvn spring-boot:run` is still pending —
this sandbox has no real provider credentials available.

## Run the app

```sh
mvn spring-boot:run
```

Runs the golden dataset through whichever judge(s) have a key and prints each quality gate's verdict.

## Why a custom `Evaluator` in addition to the LLM-as-judge ones

`RelevancyEvaluator`/`FactCheckingEvaluator` both spend a real API call per evaluation. `ConcisenessEvaluator`
costs nothing and runs instantly, because "is this answer under N words" doesn't need an LLM's judgement —
it needs `String.split(...)`. A real evaluation suite layers both kinds: cheap deterministic checks
(length, forbidden patterns, schema conformance) filtering out obvious failures before ever spending a
judge-model API call on the harder, genuinely-needs-a-brain questions (is this *relevant*? is this
*factually supported*?).

## Deviations from the roadmap wording

Same Gemini artifact deviation as every other dual-provider POC on this roadmap:
`spring-ai-starter-model-google-genai` / `GoogleGenAiChatModel`, not `VertexAiGeminiChatModel`.
