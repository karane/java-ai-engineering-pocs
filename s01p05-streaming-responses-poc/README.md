# s01p05 — Streaming Responses

Streams model output instead of waiting for the full response.

## Requirements

- Java 25
- Maven
- An Anthropic API key and/or a Gemini API key

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
export GEMINI_API_KEY=...
```

Without either variable, the app still starts and the context-load test still passes — the live streaming tests for that provider skip gracefully.

## Run the tests

```sh
mvn test
```

- No API keys: 1 test runs (context load), the rest skip.
- With both keys set: all live streaming tests run, including the chunk-granularity comparison.

## Run the app

```sh
mvn spring-boot:run
```

Console-only app — on startup it streams a short paragraph about the Moon landing from each configured provider, printing the chunk count and assembled text, plus a backpressure-collected run for Claude.

## Test

```sh
mvn test
```