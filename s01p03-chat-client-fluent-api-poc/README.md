# s01p03 — ChatClient Fluent API

Customizer.

## Requirements

- Java 25
- Maven
- An Anthropic API key

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
```

If your key is workspace-scoped, also set the real workspace ID (not its display name — see `s01p01`'s README for how to find it):

```sh
export ANTHROPIC_WORKSPACE_ID=wrkspc_...
```

## Run the tests

```sh
mvn test
```

- No `ANTHROPIC_API_KEY`: 1 test runs (both ChatClients wire up correctly), 6 skip.
- With `ANTHROPIC_API_KEY` set: all 7 run, including streaming and metadata assertions against real Claude responses.

## Run the app

```sh
mvn spring-boot:run
```

Console-only app — on startup it asks the same question four different ways (default persona, system override, no persona, full metadata) and prints each result, or explains that `ANTHROPIC_API_KEY` isn't set.

## Test

```sh
mvn test
```
