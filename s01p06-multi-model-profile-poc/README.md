# s01p06 — Multi-Model Profile

Switches the active `ChatModel` backing the app via Spring profiles instead of code changes.

## Requirements

- Java 25
- Maven
- An Anthropic API key and/or a Gemini API key

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
export GEMINI_API_KEY=...
```

Without either variable, the app still starts and the wiring/unit tests still pass — the live test for whichever provider's key is missing skips gracefully.

## Run the tests

```sh
mvn test
```

Runs both `claude`- and `gemini`-profiled test classes regardless of the default active profile — each test class pins its own profile via `@ActiveProfiles`.

## Run the app

```sh
# Claude (the default — no profile flag needed)
mvn spring-boot:run

# Gemini
mvn spring-boot:run -Dspring-boot.run.profiles=gemini
```

## Test

```sh
mvn test
```
