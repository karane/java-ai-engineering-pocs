# s02p05 — Resource-Backed Prompt Catalog

An externalized prompt library: `.st` (StringTemplate) prompt files live under `src/main/resources/prompts/` instead of being hard-coded into Java strings, and a `PromptTemplateRegistry` service loads/caches them by a short string key. Includes two versions of the same prompt (`summarize:v1` / `summarize:v2`) to demonstrate A/B comparison of prompt wording against the same input.

## Requirements

- Java 25
- Maven
- An Anthropic API key (optional — everything except the live tests and live demo calls works with zero keys)

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
```

Without it, the app still starts, the registry still loads and renders every template, and the test suite still passes — only the live Claude calls are skipped.

## Run the tests

```sh
mvn test
```

- No `ANTHROPIC_API_KEY` set: 6 tests run (context load + 5 registry unit tests against real classpath resources, no live call), 2 skip.
- `ANTHROPIC_API_KEY` set: all 8 tests run, including the live A/B comparison call.

## Run the app

```sh
mvn spring-boot:run
```

Console-only app — prints the catalog's keys, a locally-rendered `classify` prompt, and — with a key set — a live classification plus both `summarize` versions' live responses side by side.

## Test

```sh
mvn test
```
