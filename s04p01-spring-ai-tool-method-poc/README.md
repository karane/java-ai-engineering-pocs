# s04p01 — Spring AI Tool Method

Demonstrates the two ways Spring AI turns Java code into a tool the model can call — `@Tool`-annotated methods and `FunctionToolCallback.builder(...)` for a plain lambda — plus the automatic multi-turn tool execution loop that runs those tools and feeds their results back to the model without any manual re-prompting.

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

Green with zero keys set (context-load test + ungated `ToolRegistrationTests`); `ToolMethodLiveTest` reports as skipped without `ANTHROPIC_API_KEY`, and runs for real when it's exported.

## Run the app

```sh
mvn spring-boot:run
```

Console-only app — prints both providers' final answers after their respective automatic tool-execution loops complete (current date/time, arithmetic, and a Celsius→Fahrenheit conversion).

## Test

```sh
mvn test
```
