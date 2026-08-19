# s02p04 — Prompt Stuffing + Context Budgeting

Injects a block of "retrieved" context (simulating what a vector store or search index would return) into `PromptTemplate` variables alongside a user question, estimates the combined prompt's token count with a `TokenCountEstimator` before sending anything, and truncates the context — never the question — when that estimate exceeds a configured `max-tokens` budget.

## Requirements

- Java 25
- Maven
- An Anthropic API key (optional — everything except the final live call works without one)

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
```

Without it, the app still starts, both demo cases still print their token/truncation output, and the live test skips gracefully.

## Run the tests

```sh
mvn test
```

- `ContextStuffingServiceTests` — ungated unit tests for the token-estimation and truncation logic itself, no network call needed.
- `ContextStuffingServiceLiveTest` — gated on `ANTHROPIC_API_KEY`, sends a stuffed prompt to Claude and asserts a non-blank answer.

## Run the app

```sh
mvn spring-boot:run
```

Console-only app — prints the token budget, then the fits-fine and truncated demo cases with their estimated token counts, then (if `ANTHROPIC_API_KEY` is set) Claude's answer to the fits-fine prompt.

## Test

```sh
mvn test
```
