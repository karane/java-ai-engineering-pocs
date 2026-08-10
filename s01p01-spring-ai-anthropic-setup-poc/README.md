# s01p01 — Spring AI + Anthropic Claude Setup

Minimal Spring Boot app wiring Spring AI to Anthropic Claude.

## Requirements

- Java 25
- Maven
- An Anthropic API key

## Setup

Export your API key before running:

```sh
export ANTHROPIC_API_KEY=sk-ant-...
```

If your key is **workspace-scoped** (identity-linked), you'll also need its workspace ID (the actual ID, e.g. `wrkspc_...` — found in the Anthropic Console under the workspace's settings, not its display name):

```sh
export ANTHROPIC_WORKSPACE_ID=wrkspc_...
```

Without either variable set, the app still starts and the test suite still passes — the live Claude calls are skipped gracefully.

## Run the tests

```sh
mvn test
```

- No `ANTHROPIC_API_KEY` set: 1 test runs (context loads, `ChatModel`/`ChatClient` wiring verified), 2 tests skip.
- `ANTHROPIC_API_KEY` (and `ANTHROPIC_WORKSPACE_ID` if needed) set: all 3 tests run, including live calls to Claude.

## Run the app

```sh
mvn spring-boot:run
```

This is a console-only app (no web server, no endpoints) — on startup it either prints a one-line Claude response to a demo question, or a message explaining that `ANTHROPIC_API_KEY` isn't set, then exits.

## Test

```sh
mvn test
```
