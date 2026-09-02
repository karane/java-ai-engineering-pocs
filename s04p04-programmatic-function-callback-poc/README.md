# s04p04 — Programmatic Function Callback

Demonstrates registering Spring AI tools programmatically from plain `Function<I, O>`s — no `@Tool`/`@ToolParam` annotations anywhere — plus registering different tool sets per request at call time, and a multi-step reasoning chain where a tool's structured result feeds a follow-up formatted answer.

## Requirements

- Java 25
- Maven
- An Anthropic API key (tests and the app boot fine without it; live behavior is simply skipped)

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
```

## Run the tests

```sh
mvn test
```

With no keys set: context-load test plus all ungated `FunctionToolCallbackRegistrationTests` run; `ProgrammaticFunctionCallbackLiveTest` is skipped. With `ANTHROPIC_API_KEY` exported, the live test also runs.

## Run the app

```sh
mvn spring-boot:run
```

With a key, prints four live demo calls: a weather-only tool set, a stock-only tool set, a dynamically-combined tool set, and a weather-to-packing-advice multi-step chain.

## Test

```sh
mvn test
```
