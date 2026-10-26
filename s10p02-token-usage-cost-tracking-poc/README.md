# s10p02 — Token Usage & Cost Tracking

Reading `ChatResponse.getMetadata().getUsage()` for real token counts, converting them to a dollar figure
via per-provider pricing constants, publishing that cost as a `MeterRegistry` gauge tagged by provider,
and a Claude vs. Gemini cost-per-request comparison.

## What's demonstrated

- **`ChatClientConfig`** — wires `anthropicChatClient`/`geminiChatClient` beans, same dual-provider
  pattern as `s06p03`.
- **`Provider`** — enum carrying each provider's model name and pricing constants (USD per 1,000 prompt
  / completion tokens) — illustrative list prices for a small model in each family, not a live price feed.
- **`CostCalculator`** — pure function: `Usage` (prompt/completion token counts) + `Provider` → cost in
  USD. No Spring, no network — trivially unit-testable.
- **`TokenUsageResult`** — record capturing one call's answer, token counts, and cost.
- **`TokenUsageService`** — calls `ChatClient.prompt().user(...).call().chatResponse()`, reads
  `response.getMetadata().getUsage()` (`Usage.getPromptTokens()`/`getCompletionTokens()`/
  `getTotalTokens()`), computes cost via `CostCalculator`, and registers/updates a `gen_ai.client.cost.usd`
  `Gauge` tagged `provider=anthropic`/`provider=gemini` in the `MeterRegistry`.
- **`TokenUsageDemoRunner`** — asks both providers the same question (whichever has a key) and prints
  token counts and cost for each.

## Requirements

- Java 25
- Maven
- An Anthropic API key and/or a Gemini API key (Google AI Studio key) — only for the live tests and the
  demo app; everything else runs with zero keys.

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

- No keys set: `contextLoads()` plus `CostCalculatorTests` (5 unit tests, pure arithmetic, no network) run
  green — 6 tests. Both live tests skip.
- Both keys exported: `AnthropicTokenUsageLiveTest`/`GeminiTokenUsageLiveTest` also run, each asserting a
  real call returns non-zero token counts, a positive cost, and that the `gen_ai.client.cost.usd` gauge
  reflects it.

**Status in this environment:** `mvn test` with no API keys is green (6 tests, 2 skipped). Live-path
verification with real `ANTHROPIC_API_KEY`/`GEMINI_API_KEY` and `mvn spring-boot:run` is still pending —
this sandbox has no real provider credentials available.

## Run the app

```sh
mvn spring-boot:run
```

Asks one question through whichever provider(s) have a key and prints token counts plus cost for each.

## Pricing constants are illustrative, not authoritative

`Provider`'s per-1,000-token prices are a snapshot used to prove the cost-calculation mechanism works —
they are not fetched live and will drift from each vendor's actual published pricing over time. Treat
`CostCalculator` as "here's how you'd wire this up," and update `Provider`'s constants from the vendor's
current pricing page before trusting a cost figure for anything real.

## Deviations from the roadmap wording

- Same Gemini artifact deviation as `s01p02`/`s06p03`/`s10p01`: `GoogleGenAiChatModel` /
  `spring-ai-starter-model-google-genai`, not `VertexAiGeminiChatModel`.
- The roadmap text says `Usage.getPromptTokens()/getGenerationTokens()`; the actual Spring AI 1.1.8
  `Usage` interface method is `getCompletionTokens()`, not `getGenerationTokens()`. Implemented against
  the real method name.
