# s03p05 — Schema Validation + Retry

Jakarta Bean Validation (`@NotNull`, `@Size`, `@Pattern`, `@DecimalMin`/`@DecimalMax`) layered on top of a `BeanOutputConverter`-parsed record, with a retry loop that re-prompts Claude — including the exact constraint-violation messages — whenever the model's structured output fails validation. The target record also carries a self-reported `confidenceScore`.

## Requirements

- Java 25
- Maven
- An Anthropic API key (optional — everything except the live test and live demo call works with zero keys)

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
```

Without it, the app still starts, the demo runner prints a skip message, and the full test suite still passes — only the live Claude call is skipped.

## Run the tests

```sh
mvn test
```

- No `ANTHROPIC_API_KEY` set: 12 tests run (context-load, `CustomerLead` Bean Validation unit tests, retry-loop unit tests against a fake LLM), 1 skips.
- `ANTHROPIC_API_KEY` set: all 13 tests run, including the live extraction call.

## Run the app

```sh
mvn spring-boot:run
```

Console-only app — with a key, extracts and prints a validated `CustomerLead` from a hard-coded sample inquiry, including how many attempts it took.

## Test

```sh
mvn test
```
