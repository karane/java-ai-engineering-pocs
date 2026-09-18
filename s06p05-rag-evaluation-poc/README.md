# s06p05 — RAG evaluation

Stretch POC of Level 6. Builds a small, fully self-contained RAG pipeline — `SimpleVectorStore` +
`QuestionAnswerAdvisor` over a fixed four-document knowledge base — then evaluates the resulting
`ChatResponse` with the two evaluators Spring AI ships out of the box: `RelevancyEvaluator` (is the
answer in line with the retrieved context?) and `FactCheckingEvaluator` (is the answer factually
supported by the context, i.e. not hallucinated?). Both are LLM-as-judge: they build a prompt from the
question/context/answer and hand it to a *second* `ChatClient` — the judge — whose plain "yes"/"no" reply
becomes a pass/fail `EvaluationResponse`.

What's demonstrated:

- A tiny RAG pipeline (`RagAnswerService`) wrapping a `ChatClient` with `QuestionAnswerAdvisor`, backed by
  a `SimpleVectorStore` and a deterministic bag-of-words "hashing trick" fake `EmbeddingModel` — so
  retrieval genuinely finds the one relevant document per golden question, with zero API keys.
- `RelevancyEvaluator` and `FactCheckingEvaluator`, both driven by a judge `ChatClient.Builder` — a second
  chat client, separate from the one that answered the question.
- A `CannedChatModel` (a fake `ChatModel` that always returns the same canned text) standing in for both
  the answerer and the judge in the default, offline test path — proving the whole
  retrieve → answer → evaluate → assert chain with zero API keys.
- JUnit 5 regression tests: an offline, parameterized test asserting `EvaluationResponse.isPass()` over a
  small golden question/answer set against a fake judge, plus live, per-provider tests
  (`RagEvaluationAnthropicJudgeLiveTest`, `RagEvaluationGeminiJudgeLiveTest`) asserting the same thresholds
  against a real Anthropic or Gemini judge model.

## Requirements

- Java 25, Maven
- `ANTHROPIC_API_KEY` and/or `GEMINI_API_KEY` for the live per-provider judge tests (both optional —
  `mvn test` is fully green with neither set)

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

If your active SDKMAN Java isn't 25, prefix with `JAVA_HOME`/`PATH` as in the repo's root `CLAUDE.md`.

- No API keys set: context-load test + `RagEvaluationOfflineTests` (6 tests) run — these use
  `CannedChatModel` as both the RAG answerer and the judge, so the evaluator plumbing and the
  threshold-assertion logic are proven with zero keys. Both live test classes skip.
- With `ANTHROPIC_API_KEY` set: `RagEvaluationAnthropicJudgeLiveTest` also runs, asserting a real Claude
  judge passes every golden case's grounded answer and fails a deliberately ungrounded one.
- With `GEMINI_API_KEY` set: `RagEvaluationGeminiJudgeLiveTest` runs the same assertions against a real
  Gemini judge.

## Run the app

```sh
mvn spring-boot:run
```

Console-only. Always runs the offline demo (fake answerer + fake PASS judge over the golden questions).
With `ANTHROPIC_API_KEY` set, it additionally re-judges one case with the real Claude model as a live
sanity check.

## Gotcha: Claude's "YES" often comes with an unwanted explanation

Both evaluators' default prompts ask for a bare `YES`/`NO` answer, then parse the judge's reply with a
strict `equalsIgnoreCase` — not a "contains" check. Claude, unlike some other models, tends to follow up
`YES` with an explanation even when told not to elaborate:

```
YES

The response "The Eiffel Tower was completed in 1889" is in line with the context information
provided, which states "The Eiffel Tower was completed in 1889 for the World's Fair in Paris."
```

That fails the strict equality check and silently flips a genuinely correct answer into a `FAIL` with
`score=0.0` — no exception, just a wrong verdict. `RagEvaluationService` works around this by overriding
both evaluators' default prompt templates with an explicit "respond with exactly one word and nothing
else" instruction, which reliably gets clean, parseable verdicts out of Claude, Gemini, and the fake judge
model alike. Worth knowing before trusting either evaluator's default prompt against a model you haven't
tested it with.

## Gotcha: a token-overlap fake embedding, not a whole-string hash

`s05p03`'s fake `EmbeddingModel` hashes the *entire string* into a vector — enough to prove `add`/
`search`/`filter`/`save`/`load` mechanics, but the resulting vectors carry no real semantic relationship
between texts (two unrelated sentences are just as likely to look "similar" as two related ones). Feeding
that into a *real* LLM judge, alongside 3 unrelated documents, produced confusing prompts and made
`RelevancyEvaluator` genuinely uncertain — the answer only addressed one of the four documents in its
context. This POC's `DeterministicFakeEmbeddingModel` instead uses the classic "hashing trick": each word
is lower-cased and hashed into one of 64 buckets, and the bucket's count is incremented. Two texts sharing
words like "Eiffel"/"Tower" end up with vectors pointing in a similar direction; texts sharing no words
don't. That's enough real keyword-overlap signal for `topK(1)` to reliably retrieve the one genuinely
relevant document per golden question — which matters once a *real* judge model is reading that context.

## Key API Reference

| API | Purpose |
|---|---|
| `RelevancyEvaluator.builder().chatClientBuilder(...).promptTemplate(...)` | LLM-as-judge: is the answer in line with the context? |
| `FactCheckingEvaluator.builder(...).evaluationPrompt(...)` | LLM-as-judge: is the answer supported by the context (no hallucination)? |
| `new EvaluationRequest(userText, List<Document>, responseContent)` | Bundles question + retrieved context + answer for an evaluator |
| `EvaluationResponse.isPass()` / `.getScore()` | The evaluator's pass/fail verdict and score |
| `QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS` | Metadata key under which the advisor stores retrieved docs on the `ChatResponse` |
| `ChatClient.Builder` (a second one) | The "judge" model, separate from the one that answered the question |

## What this proves

- `RelevancyEvaluator` and `FactCheckingEvaluator` are real, provable LLM-as-judge evaluators — not just
  API surface — verified against both a fake judge (plumbing) and real Anthropic/Gemini judges (actual
  judgement).
- The whole evaluate-and-assert chain (retrieve → answer → evaluate → threshold assertion) is provable
  with zero API keys, using a canned `ChatModel` standing in for both the answerer and the judge.
- Evaluator prompt defaults are not one-size-fits-all across providers — Claude's tendency to explain its
  "YES" answers is a real, reproducible gotcha that a stricter custom prompt template fixes.
- "Per provider" regression testing for LLM-as-judge scoring is a straightforward pattern: one
  `RagEvaluationService`, constructed with different `ChatClient.Builder`s, run against the same golden
  dataset.

Next on the roadmap: `s06p06-contextual-compression-rag-poc` — post-retrieval reranking via a
cross-encoder-style `ChatClient` scoring call, `DocumentPostProcessor` filtering low-relevance chunks, and
injected-context token budgeting per provider's context window.
