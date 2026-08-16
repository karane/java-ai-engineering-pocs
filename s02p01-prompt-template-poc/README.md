# s02p01 — Prompt Template

Spring AI's `PromptTemplate` abstraction: building a template from an inline string with `PromptTemplate.builder().template(String).build()`.

## Requirements

- Java 25
- Maven
- An Anthropic API key (only needed for the live call/test — template rendering itself needs no key)

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
```

Without it, the app still starts, both templates still render and print, and only the final live Claude call is skipped.

## Run the tests

```sh
mvn test
```

## Run the app

```sh
mvn spring-boot:run
```

Console-only app — on startup it prints the rendered inline template, the rendered resource-loaded template, and (if a key is set) Claude's answer to the rendered prompt.

## Test

```sh
mvn test
```