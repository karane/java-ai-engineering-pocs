# s02p02 — System Prompt Persona

`SystemPromptTemplate`-driven persona construction, explicit `Message` types (`SystemMessage`, `UserMessage`, `AssistantMessage`), hand-built multi-turn `Prompt(List<Message>)` construction, and a side-by-side comparison of how consistently Claude and Gemini honor the same system prompt.

## Requirements

- Java 25
- Maven
- An Anthropic API key and/or a Gemini API key

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
export GEMINI_API_KEY=...
```

Without either variable, the app still starts and the unit/context tests still pass — the live test for whichever provider's key is missing skips gracefully, and the demo runner prints a skip message per provider instead of failing.

## Run the tests

```sh
mvn test
```

## Run the app

```sh
mvn spring-boot:run
```

Console-only app — on startup it asks the "terse code reviewer" persona the same question of both providers (when their keys are set), runs a hand-built multi-turn exchange, and prints a side-by-side comparison when both keys are present.

## Test

```sh
mvn test
```
