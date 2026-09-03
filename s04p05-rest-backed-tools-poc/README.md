# s04p05 — REST-Backed Tools

`@Tool`-annotated methods wrapping `RestClient` calls to a real, free, key-free external API (Open-Meteo weather + geocoding), JSON-RPC-style structured error propagation back to the model instead of uncaught exceptions, and sandboxing tool execution with explicit connect/read timeouts.

## Requirements

- Java 25
- Maven
- No API key needed for the weather tool itself; an Anthropic API key is only needed for the LLM + tool round-trip half of the demo and the gated live test

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
```

## Run the tests

```sh
mvn test
```

Green with zero keys set — `contextLoads()` plus all three `WeatherServiceTests` hit the real, key-free Open-Meteo API directly; `RestBackedToolsLiveTest` skips without `ANTHROPIC_API_KEY`.

## Run the app

```sh
mvn spring-boot:run
```

Prints three direct tool-call demo cases (success, unknown-city error, forced timeout), then, with a key set, two live LLM + tool round trips.

## Test

```sh
mvn test
```
