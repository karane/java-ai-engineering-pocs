# s03p04 — Claude Tool Use vs. Gemini JSON Mode

Compares each provider's native structured-output mechanism — Anthropic forced tool-use (`AnthropicChatOptions.toolChoice` pinning Claude to one tool, so the tool-call arguments are the structured output) and Gemini's native JSON mode (`responseMimeType("application/json")` + `responseSchema(...)`) — against a prompt-engineered `BeanOutputConverter` baseline, all extracting the same `Invoice` record.

## Requirements

- Java 25
- Maven
- An Anthropic API key and/or a Gemini API key (neither required to build or run `mvn test`)

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
export GEMINI_API_KEY=...
```

## Run the tests

```sh
mvn test
```

Green with zero keys set (context-load test + `StructuredOutputOptionsTests` unit tests); the two `*LiveTest` classes skip without their respective key and run for real when it's exported.

## Run the app

```sh
mvn spring-boot:run
```

Console-only app — prints the forced tool-use, Gemini native JSON mode, and prompt-engineered JSON extraction results for whichever provider(s) have a key set.

## Test

```sh
mvn test
```
