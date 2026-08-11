# s01p02 — Spring AI + Gemini Setup

Minimal Spring Boot app wiring Spring AI to Google Gemini via the Gemini Developer API (API-key auth, no GCP project/ADC needed).

Mirrors `s01p01`'s structure exactly, so the two are easy to compare side by side.

**Note on Spring AI version:** this POC is pinned to Spring AI **1.1.8**, not 2.0.1 like the Anthropic POCs in this repo.

## Requirements

- Java 25
- Maven
- A Gemini API key (from [Google AI Studio](https://aistudio.google.com/apikey) — a plain API key, not a GCP service account)

## Setup

```sh
export GEMINI_API_KEY=AQ...
```

## Run the tests

```sh
mvn test
```

- No `GEMINI_API_KEY`: 1 test runs (context loads, `ChatModel`/`ChatClient` wiring verified), 2 skip.
- With `GEMINI_API_KEY` set: all 3 run, including live calls to Gemini.

## Run the app

```sh
mvn spring-boot:run
```

Console-only app — on startup it either prints a one-line Gemini response to a demo question, or explains that `GEMINI_API_KEY` isn't set, then exits.

## Java version mismatch

```sh
mvn test
```
