# s04p03 — Claude vs. Gemini Tool Schema

Compares the tool JSON schema Anthropic Claude and Google Gemini each actually receive on the wire for the same `@Tool`-annotated method, plus parallel tool-call handling and forced tool-choice differences between the two providers.

## Requirements

- Java 25
- Maven
- An Anthropic API key and a Gemini API key (a Google AI Studio API key — no GCP project/ADC needed)

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
export GEMINI_API_KEY=...
```

## Run the tests

```sh
mvn test
```

- No keys set: `contextLoads()` and the ungated `ToolSchemaGenerationTests` run, the two `*LiveTest` classes skip.
- Both keys exported: all tests run and pass, including live calls to both providers.

## Run the app

```sh
mvn spring-boot:run
```

Prints both providers' reconstructed tool schemas unconditionally, then runs the parallel-tool-call and forced/restricted-tool-choice demos for whichever provider(s) have a key set.

## Test

```sh
mvn test
```
