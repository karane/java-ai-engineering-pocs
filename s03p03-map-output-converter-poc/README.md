# s03p03 — Map Output Converter

`MapOutputConverter` for untyped key-value extraction, `ListOutputConverter` for comma-delimited list parsing, and choosing/adapting converters based on Claude's vs. Gemini's JSON-mode reliability.

## Requirements

- Java 25
- Maven
- An Anthropic API key and/or a Gemini API key

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
export GEMINI_API_KEY=...
```

Without either variable, the app still starts and the wiring/unit tests still pass — the live test for whichever provider's key is missing skips gracefully, and the demo runner prints a skip message instead of calling that provider.

## Run the tests

```sh
mvn test
```

`MapOutputConverterApplicationTests` (context load) and `ExtractionServiceConverterUnitTest` (converter parsing + fence-stripping logic, no live call) always run; `ClaudeExtractionLiveTest`/`GeminiExtractionLiveTest` skip without their respective key.

## Run the app

```sh
mvn spring-boot:run
```

Console-only app — on startup it runs the `MapOutputConverter`/`ListOutputConverter` demo against Claude, then against Gemini, printing each result (or a skip message per provider if its key isn't set).

## Test

```sh
mvn test
```
