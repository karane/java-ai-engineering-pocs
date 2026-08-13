# s01p04 — Chat Options Per Provider

Tunes generation behavior per request instead of only at the client level

## Requirements

- Java 25
- Maven
- An Anthropic API key and/or a Gemini API key

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
export GEMINI_API_KEY=...
```

Without either variable, the app still starts and the wiring/unit tests still pass — the live tests for that provider skip gracefully.

## Run the tests

```sh
mvn test
```

## Run the app

```sh
mvn spring-boot:run
```

## Test

```sh
mvn test
```
