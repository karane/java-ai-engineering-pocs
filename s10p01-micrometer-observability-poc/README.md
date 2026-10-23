# s10p01 — Micrometer Observability

Spring AI's built-in `ObservationRegistry` instrumentation on `ChatClient`/`ChatModel` calls, a customized
`ChatModelObservationConvention`, and OpenTelemetry span export via a console logging exporter — plus a
tiny per-provider latency dashboard read straight off `MeterRegistry`.

## What's demonstrated

- **`ChatClientConfig`** — wires `anthropicObservedChatClient` and `geminiObservedChatClient` beans from
  the auto-configured `AnthropicChatModel`/`GoogleGenAiChatModel`, calling
  `chatModel.setObservationConvention(...)` on each so the custom convention applies to every call.
- **`PocChatModelObservationConvention`** — extends Spring AI's `DefaultChatModelObservationConvention`
  and adds one extra low-cardinality tag (`poc.module=s10p01`) to every chat observation, proving the
  convention-customization extension point is actually consulted.
- **`ObservabilityConfig`** — registers an OpenTelemetry `SpanExporter` bean
  (`LoggingSpanExporter`), which Spring Boot's Micrometer-Tracing + OTel autoconfiguration automatically
  wraps in a `BatchSpanProcessor` on the `SdkTracerProvider` — no manual provider/processor wiring needed.
- **`ObservabilityChatService`** — thin provider-agnostic `ChatClient` wrapper; deliberately does nothing
  observability-specific, since the instrumentation lives entirely inside the `ChatModel`.
- **`ObservabilityDemoRunner`** — exercises both providers (whichever has a key), then prints a
  per-provider latency "dashboard" (`count`/`meanMs`/`maxMs`) read from the `gen_ai.client.operation`
  timer in `MeterRegistry`, tagged by `gen_ai.system` (Spring AI's provider tag).

## Requirements

- Java 25
- Maven
- An Anthropic API key and/or a Gemini API key (Google AI Studio key — no GCP project/ADC needed) — only
  for the live tests and the demo app; everything else runs with zero keys.

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
export GEMINI_API_KEY=...
```

## Java version

The active SDKMAN Java was 21 when this POC was built, but the `pom.xml` targets Java 25. Prefix every
`mvn` command:

```sh
JAVA_HOME=~/.sdkman/candidates/java/25.0.3-tem \
PATH=~/.sdkman/candidates/java/25.0.3-tem/bin:$PATH \
mvn test
```

## `.gitignore`

No POC-local `.gitignore` needed — the repo root's already excludes `target/` and friends for every
module.

## Run the tests

```sh
JAVA_HOME=~/.sdkman/candidates/java/25.0.3-tem PATH=~/.sdkman/candidates/java/25.0.3-tem/bin:$PATH mvn test
```

- No keys set: `contextLoads()` plus `PocChatModelObservationConventionTests` (2 unit tests, no network)
  run — 3 tests green. Both live tests skip.
- Both keys exported: `AnthropicObservabilityLiveTest`/`GeminiObservabilityLiveTest` also run, each
  asserting a real call leaves behind a `gen_ai.client.operation` timer tagged `poc.module=s10p01`.

**Status in this environment:** `mvn test` with no API keys is green (5 tests, 2 skipped). Live-path
verification with real `ANTHROPIC_API_KEY`/`GEMINI_API_KEY` and `mvn spring-boot:run` is still pending —
this sandbox has no real provider credentials available.

## Run the app

```sh
mvn spring-boot:run
```

Asks one question through whichever provider(s) have a key, then prints the latency dashboard. Watch for
`[s10p01]`-prefixed lines (this POC's own output) alongside OpenTelemetry's own logger output (the
exported spans, printed by `LoggingSpanExporter` via `java.util.logging`, not `[s10p01]`-prefixed).

## Deviations from the roadmap wording

Same deviation as `s01p02`/`s06p03`: the roadmap names `VertexAiGeminiChatModel`, but Spring AI BOM 1.1.8
ships Gemini support as `spring-ai-starter-model-google-genai` / `GoogleGenAiChatModel` (API-key auth, not
Vertex AI/ADC). "OpenTelemetry span export" is implemented via a console `LoggingSpanExporter` rather than
an OTLP exporter pointed at a real collector, so the POC stays runnable and testable with zero external
infrastructure — swapping in `OtlpGrpcSpanExporter` for a real collector is a one-bean change in
`ObservabilityConfig`.
