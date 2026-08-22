# s03p02 — ChatClient.entity() Structured Output

Demonstrates Spring AI's `ChatClient.entity()` API for parsing a model's JSON reply straight into typed Java objects — no hand-written JSON parsing, no manual `BeanOutputConverter` wiring. Covers both the single-object overload and the generic-collection overload needed to extract a `List<T>` from one response, plus `@JsonPropertyDescription` field hints that steer the generated JSON schema.

## Requirements

- Java 25
- Maven
- An Anthropic API key (optional — everything except the live tests and live demo calls works with zero keys)

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
```

Without it, the app still starts and the test suite still passes — only the live Claude calls (and the two live-only demo prints) are skipped.

## Run the tests

```sh
mvn test
```

- No `ANTHROPIC_API_KEY` set: 3 tests run (context load + 2 `Product` record/annotation unit tests), 2 skip.
- `ANTHROPIC_API_KEY` set: all 5 tests run, including the live single-entity and list-entity extraction calls against Claude.

## Run the app

```sh
mvn spring-boot:run
```

Console-only app — with a key set, prints one extracted `Product` and a list of three extracted `Product`s from a single Claude response.

## Test

```sh
mvn test
```
