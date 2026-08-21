# s03p01 — Bean Output Converter

Turns a Claude free-text response into a typed Java `record` via `BeanOutputConverter<T>`, with a retry loop that re-prompts the model when it replies with malformed JSON.

## Requirements

- Java 25
- Maven
- An Anthropic API key (optional — everything except the live demo run and live test works with zero keys)

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
```

Without it, the app still starts, prints the injected format instructions, and exits gracefully — only the live Claude calls are skipped.

## Run the tests

```sh
mvn test
```

The context-load test, ungated `BeanOutputConverter` mapping tests, and ungated retry-loop tests all pass with zero keys; `MovieReviewExtractionServiceLiveTest` is gated on `ANTHROPIC_API_KEY` and skips without it.

## Run the app

```sh
mvn spring-boot:run
```

Console-only app — prints the format instructions on every run, and (only when a key is set) runs a live extraction and prints the resulting `MovieReview` record.

## Test

```sh
mvn test
```
