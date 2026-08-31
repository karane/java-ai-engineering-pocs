# s04p02 — Tool Context

Demonstrates `ToolContext` for injecting request-scoped runtime state (a "current user id") into a tool method without ever exposing it to the model's JSON schema or prompt text, `@ToolParam(description = ...)` for parameter-level schema hints, and `ToolCallingManager` for driving tool execution manually instead of relying on `ChatClient`'s automatic loop.

## Requirements

- Java 25
- Maven
- An Anthropic API key (not required to build or run `mvn test`)

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
```

## Run the tests

```sh
mvn test
```

Green with zero keys set (context-load test + ungated `AccountLookupToolTests`); `ToolContextLiveTest` reports as skipped without a key, and exercises both the automatic and manual tool loops when one is exported.

## Run the app

```sh
mvn spring-boot:run
```

Console-only app — with a key, prints both the automatic-loop answer and the manual-loop answer, each personalized purely from the `ToolContext` map, never from the prompt text.

## Test

```sh
mvn test
```
